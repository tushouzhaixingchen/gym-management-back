package com.gym.management.service.impl;
import com.gym.management.common.exception.BusinessException;
import com.gym.management.common.ResultCode;
import com.gym.management.dto.request.admin.AdminAppointmentQueryRequest;
import com.gym.management.dto.response.AdminAppointmentResponse;
import com.gym.management.dto.request.admin.AppointmentConfirmRequest;
import com.gym.management.dto.request.admin.NoShowRequest;
import com.gym.management.dto.request.member.AppointmentCreateRequest;
import com.gym.management.dto.request.member.CoachListRequest;
import com.gym.management.dto.request.member.PaymentRequest;
import com.gym.management.dto.response.*;
import com.gym.management.entity.Admin;
import com.gym.management.entity.Appointment;
import com.gym.management.entity.Coach;
import com.gym.management.entity.CoachSchedule;
import com.gym.management.entity.Member;
import com.gym.management.entity.Order;
import com.gym.management.entity.Store;
import com.gym.management.repository.AdminRepository;
import com.gym.management.repository.AppointmentRepository;
import com.gym.management.repository.CoachRepository;
import com.gym.management.repository.CoachScheduleRepository;
import com.gym.management.repository.MemberRepository;
import com.gym.management.repository.OrderRepository;
import com.gym.management.repository.StoreRepository;
import com.gym.management.security.GymUserDetails;
import com.gym.management.security.UserContext;
import com.gym.management.service.AppointmentService;
import com.gym.management.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private final AppointmentRepository appointmentRepository;
    private final CoachScheduleRepository scheduleRepository;
    private final CoachRepository coachRepository;
    private final OrderService orderService;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;
    private final AdminRepository adminRepository;

    // 构造器注入
    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  CoachScheduleRepository scheduleRepository,
                                  CoachRepository coachRepository,
                                  OrderService orderService,
                                  MemberRepository memberRepository,
                                  StoreRepository storeRepository,
                                  OrderRepository orderRepository,
                                  AdminRepository adminRepository) {
        this.appointmentRepository = appointmentRepository;
        this.scheduleRepository = scheduleRepository;
        this.coachRepository = coachRepository;
        this.orderService = orderService;
        this.memberRepository = memberRepository;
        this.storeRepository = storeRepository;
        this.orderRepository = orderRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public List<CoachResponse> getCoachList(CoachListRequest request) {
        log.info("========== 查询教练列表 ==========");
        log.info("请求参数：storeId={}, name={}, gender={}, isAvailable={}", 
            request.getStoreId(), request.getName(), request.getGender(), request.getIsAvailable());
        
        List<Coach> coaches = coachRepository.findAll();
        log.info("从数据库查询到 {} 条教练记录", coaches.size());
        
        if (coaches.isEmpty()) {
            log.warn("数据库中没有教练数据，请先插入测试数据！");
        } else {
            for (Coach coach : coaches) {
                log.info("教练信息：ID={}, 工号={}, 姓名={}, 性别={}, 状态={}", 
                    coach.getId(), coach.getCoachNo(), coach.getRealName(), 
                    coach.getGender(), coach.getStatus());
            }
        }

        return coaches.stream().map(coach -> {
            CoachResponse response = new CoachResponse();
            BeanUtils.copyProperties(coach, response);
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public CoachResponse getCoachDetail(Integer coachId) {
        log.info("查询教练详情，coachId: {}", coachId);
        
        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new RuntimeException("教练不存在"));
        
        log.info("找到教练：{}, 姓名：{}", coach.getCoachNo(), coach.getRealName());
        
        CoachResponse response = new CoachResponse();
        BeanUtils.copyProperties(coach, response);
        
        // 设置文本信息
        response.setGenderText(getGenderText(coach.getGender()));
        response.setStatusText(getStatusText(coach.getStatus()));
        response.setLevelText(getLevelText(coach.getLevel()));
        
        // 判断是否在职（状态为 1 才是在职）
        response.setIsAvailable(coach.getStatus() == 1);
        
        return response;
    }

    // 辅助方法：获取性别文本
    private String getGenderText(Integer gender) {
        if (gender == null) return "未知";
        return switch (gender) {
            case 1 -> "男";
            case 2 -> "女";
            default -> "未知";
        };
    }

    // 辅助方法：获取状态文本
    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "在职";
            case 0 -> "离职";
            case 2 -> "休假";
            default -> "未知";
        };
    }

    // 辅助方法：获取等级文本
    private String getLevelText(String level) {
        if (level == null) return "未知等级";
        return switch (level) {
            case "junior" -> "初级教练";
            case "middle" -> "中级教练";
            case "senior" -> "高级教练";
            default -> "未知等级";
        };
    }

    @Override
    public List<TimeSlotResponse> getCoachAvailableSlots(Integer coachId, LocalDate date) {
        // 1. 获取排班
        CoachSchedule schedule = scheduleRepository.findByCoachAndDate(coachId, date)
                .orElseThrow(() -> new RuntimeException("该教练当天无排班"));

        if (schedule.getStatus() == 0) {
            return new ArrayList<>(); // 休息日
        }

        // 2. 获取已占用时段 (状态为待确认或已确认的)
        List<Appointment> occupiedList = appointmentRepository.findOccupiedSlots(
                coachId,
                LocalDateTime.of(date, schedule.getStartTime()),
                LocalDateTime.of(date, schedule.getEndTime())
        );

        // 3. 切割时间槽
        List<TimeSlotResponse> slots = new ArrayList<>();
        LocalTime currentStart = schedule.getStartTime();

        for (Appointment appt : occupiedList) {
            LocalTime apptStart = appt.getTimeSlotStart().toLocalTime();
            LocalTime apptEnd = appt.getTimeSlotEnd().toLocalTime();

            if (currentStart.isBefore(apptStart)) {
                slots.add(new TimeSlotResponse(currentStart.toString(), apptStart.toString()));
            }
            // 移动指针到当前预约结束时间
            if (apptEnd.isAfter(currentStart)) {
                currentStart = apptEnd;
            }
        }

        // 添加最后一段空闲时间
        if (currentStart.isBefore(schedule.getEndTime())) {
            slots.add(new TimeSlotResponse(currentStart.toString(), schedule.getEndTime().toString()));
        }

        return slots;
    }

    @Override
    @Transactional
    public AppointmentResponse createAppointment(AppointmentCreateRequest request, Integer memberId) {
        log.info("开始创建预约 | 教练 ID: {}, 会员 ID: {}, 日期：{}, 时间：{} - {}", 
                request.getCoachId(), memberId, request.getDate(), request.getStartTime(), request.getEndTime());
            
        // 0. 获取完整的日期时间
        LocalDateTime startTime = request.getStartDateTime();
        LocalDateTime endTime = request.getEndDateTime();
            
        log.info("转换后的时间 | 开始：{}, 结束：{}", startTime, endTime);
            
        // 1. 校验时长 (至少 1 小时)
        long minutes = Duration.between(startTime, endTime).toMinutes();
        log.info("预约时长：{} 分钟", minutes);
        if (minutes < 60) {
            log.warn("预约时长不足 | 时长：{} 分钟", minutes);
            throw new BusinessException(ResultCode.APPOINTMENT_INVALID_DURATION);
        }
    
        // 2. 校验冲突 (双重检查)
        log.info("检查时段冲突 | 教练 ID: {}, 时间段：{} 到 {}", request.getCoachId(), startTime, endTime);
        boolean hasConflict = appointmentRepository.existsByCoachIdAndTimeSlotBetween(
                request.getCoachId(), startTime, endTime
        );
        log.info("冲突检查结果：{}", hasConflict ? "存在冲突" : "无冲突");
        if (hasConflict) {
            log.warn("预约冲突 | 教练 ID: {}, 时间段：{} - {}", request.getCoachId(), startTime, endTime);
            throw new BusinessException(ResultCode.APPOINTMENT_CONFLICT);
        }
    
        // 3. 获取教练信息，获取课时费
        Coach coach = coachRepository.findById(request.getCoachId())
                .orElseThrow(() -> new BusinessException(ResultCode.COACH_NOT_FOUND));
            
        // 4. 创建实体
        Appointment appointment = new Appointment();
        appointment.setAppointmentNo("APPT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        appointment.setMemberId(memberId);
        appointment.setCoachId(request.getCoachId());
        appointment.setStoreId(request.getStoreId());
        appointment.setTimeSlotStart(startTime);
        appointment.setTimeSlotEnd(endTime);
        appointment.setDurationMinutes((int) minutes);
        // 从教练的 hourly_rate 获取价格
        appointment.setPrice(coach.getHourlyRate() != null ? coach.getHourlyRate() : new java.math.BigDecimal("0.00"));
        appointment.setPurpose(request.getPurpose());
        appointment.setStatus(0); // 待确认
        appointment.setPayStatus(0); // 未支付
        appointment.setCreatedAt(LocalDateTime.now());
    
        Appointment saved = appointmentRepository.save(appointment);
    
        // 转换为 Response
        return convertToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments(Integer memberId) {
        log.info("========== 查询会员预约列表 | memberId: {} ==========", memberId);
        List<Appointment> list = appointmentRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        log.info("查询到 {} 条预约记录", list.size());
        
        for (Appointment appt : list) {
            log.info("预约 ID: {}, 编号：{}, 状态：{} ({}), 支付状态：{} ({})",
                appt.getId(), appt.getAppointmentNo(),
                appt.getStatus(), getStatusDesc(appt.getStatus()),
                appt.getPayStatus(), getPayStatusDesc(appt.getPayStatus()));
        }
        
        return list.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AppointmentResponse payAppointment(Integer appointmentId, PaymentRequest request) {
        log.info("========== 支付预约 | appointmentId: {} ==========", appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ResultCode.APPOINTMENT_NOT_FOUND));

        // 校验状态：必须是已确认且未支付
        if (appointment.getStatus() != 1) {
            throw new BusinessException(ResultCode.APPOINTMENT_STATUS_INVALID);
        }
        if (appointment.getPayStatus() == 1) {
            throw new BusinessException(ResultCode.APPOINTMENT_ALREADY_PAID);
        }

        // 更新支付信息
        appointment.setPayStatus(1);
        appointment.setPayMethod(request.getPayMethod());
        appointment.setPayTime(LocalDateTime.now());

        // 保存预约状态
        appointmentRepository.saveAndFlush(appointment);

        // 生成订单记录
        orderService.createOrder(appointment);

        // 构建响应，填充完整信息
        AppointmentResponse response = new AppointmentResponse();
        BeanUtils.copyProperties(appointment, response);
        
        // 设置状态描述
        response.setStatusDesc(getStatusDesc(appointment.getStatus()));
        response.setPayStatusDesc(getPayStatusDesc(appointment.getPayStatus()));
        
        // 查询教练信息（包括价格）
        try {
            Coach coach = coachRepository.findById(appointment.getCoachId()).orElse(null);
            if (coach != null) {
                response.setCoachName(coach.getRealName());
                // 确保价格使用教练的课时费
                if (coach.getHourlyRate() != null) {
                    response.setPrice(coach.getHourlyRate());
                }
            }
        } catch (Exception e) {
            log.warn("查询教练信息失败 | coachId: {}", appointment.getCoachId(), e);
        }
        
        // 查询门店信息
        try {
            Store store = storeRepository.findById(appointment.getStoreId()).orElse(null);
            if (store != null) {
                response.setStoreName(store.getStoreName());
            }
        } catch (Exception e) {
            log.warn("查询门店信息失败 | storeId: {}", appointment.getStoreId(), e);
        }
        
        log.info("支付预约成功 | appointmentId: {}, 预约编号：{}, 价格：{}", 
            appointmentId, appointment.getAppointmentNo(), response.getPrice());
        
        return response;
    }

    // --- 管理员端实现 ---

    /**
     * 获取当前登录管理员的 ID（从 SecurityContext）
     */
    private Integer getCurrentAdminId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (principal instanceof Integer) {
            return (Integer) principal;
        } else {
            log.warn("【权限检查】Principal 不是 Integer 类型：{}", principal.getClass().getName());
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminAppointmentResponse> getAdminAppointments(AdminAppointmentQueryRequest request) {
        log.info("========== 查询管理员端预约列表 ==========");
        
        // 🔴 直接从 SecurityContext 获取当前登录管理员 ID（Integer 类型）
        Integer currentAdminId = getCurrentAdminId();
        log.info("【权限检查】当前登录管理员 ID: {}", currentAdminId);
        
        // 从数据库查询管理员信息，获取门店 ID 和角色 ID
        Integer currentAdminStoreId = null;
        Integer currentAdminRoleId = null;
        boolean isSuperAdmin = false;
        String currentUsername = null;
        
        if (currentAdminId != null) {
            Admin admin = adminRepository.findById(currentAdminId).orElse(null);
            if (admin != null) {
                currentUsername = admin.getUsername();
                currentAdminStoreId = admin.getStoreId();
                currentAdminRoleId = admin.getRoleId();
                // roleId=1 为超级管理员
                isSuperAdmin = (admin.getRoleId() != null && admin.getRoleId() == 1);
                log.info("【权限检查】管理员信息 - id={}, username={}, storeId={}, roleId={}, isSuperAdmin={}", 
                    currentAdminId, currentUsername, currentAdminStoreId, currentAdminRoleId, isSuperAdmin);
            } else {
                log.warn("【权限检查】未找到管理员信息：id={}", currentAdminId);
            }
        } else {
            log.warn("【权限检查】未获取到当前登录管理员 ID");
        }
        
        log.info("【权限检查】当前管理员门店 ID: {}, 角色 ID: {}, 是否超级管理员：{}", 
            currentAdminStoreId, currentAdminRoleId, isSuperAdmin);
        
        log.info("请求参数：storeId={}, status={}, keyword={}, startDate={}, endDate={}", 
            request.getStoreId(), request.getStatus(), request.getKeyword(), 
            request.getStartDate(), request.getEndDate());
        
        // 确定最终使用的门店 ID
        // 规则：
        // 1. 超级管理员：可以使用前端传的 storeId，也可以不传（查询所有）
        // 2. 普通管理员：只能查询本店，忽略前端传的 storeId，强制使用当前管理员的 storeId
        Integer finalSTOREId = null;
        
        if (isSuperAdmin) {
            // 超级管理员可以查询所有门店或指定门店
            finalSTOREId = request.getStoreId();
            if (finalSTOREId == null) {
                log.info("【权限检查】✓ 超级管理员模式：查询所有门店");
            } else {
                log.info("【权限检查】✓ 超级管理员模式：查询指定门店 ID={}", finalSTOREId);
            }
        } else if (currentAdminStoreId != null) {
            // 普通管理员只能查询本店
            finalSTOREId = currentAdminStoreId;
            log.info("【权限检查】✓ 门店管理员模式：只能查询本店 ID={}", finalSTOREId);
        } else {
            log.warn("【权限检查】✗ 当前用户既不是超级管理员也没有门店 ID，无权访问");
        }
        
        final Integer effectiveStoreId = finalSTOREId;
        
        log.info("【权限检查】最终用于筛选的门店 ID: {}", effectiveStoreId);
        
        // 查询所有预约
        List<Appointment> list = appointmentRepository.findAll();
        log.info("从数据库查询到 {} 条预约记录", list.size());
        
        // 打印所有预约的门店 ID（保留原有调试日志）
        for (Appointment appt : list) {
            log.info("【调试日志】预约 ID={}, 预约单号={}, 所属门店 ID={}", 
                appt.getId(), appt.getAppointmentNo(), appt.getStoreId());
        }
        
        // 在内存中过滤数据（适合数据量不大的情况）
        return list.stream()
            // 筛选门店（普通管理员只能查看本店，超级管理员可以查看所有或指定店）
            .filter(appt -> {
                boolean pass = effectiveStoreId == null || appt.getStoreId().equals(effectiveStoreId);
                if (effectiveStoreId != null) {
                    log.info("【调试日志】门店筛选：预约 ID={}, 预约 storeId={}, 请求 storeId={}, 通过={}", 
                        appt.getId(), appt.getStoreId(), effectiveStoreId, pass);
                } else {
                    log.info("【调试日志】门店筛选：effectiveStoreId 为 null，跳过筛选，预约 ID={}, 通过={}", 
                        appt.getId(), pass);
                }
                return pass;
            })
            // 筛选状态
            .filter(appt -> {
                boolean pass = request.getStatus() == null || appt.getStatus().equals(request.getStatus());
                log.debug("状态筛选：预约 ID={}, 预约 status={}, 请求 status={}, 通过={}", 
                    appt.getId(), appt.getStatus(), request.getStatus(), pass);
                return pass;
            })
            // 筛选日期范围
            .filter(appt -> {
                boolean pass = request.getStartDate() == null || 
                        !appt.getTimeSlotStart().isBefore(request.getStartDate());
                if (request.getStartDate() != null) {
                    log.debug("日期筛选 (start)：预约 ID={}, 预约时间={}, 请求开始时间={}, 通过={}", 
                        appt.getId(), appt.getTimeSlotStart(), request.getStartDate(), pass);
                }
                return pass;
            })
            .filter(appt -> {
                boolean pass = request.getEndDate() == null || 
                        !appt.getTimeSlotStart().isAfter(request.getEndDate());
                if (request.getEndDate() != null) {
                    log.debug("日期筛选 (end)：预约 ID={}, 预约时间={}, 请求结束时间={}, 通过={}", 
                        appt.getId(), appt.getTimeSlotStart(), request.getEndDate(), pass);
                }
                return pass;
            })
            // 关键词搜索（会员名或预约号）
            .filter(appt -> {
                if (request.getKeyword() == null || request.getKeyword().isEmpty()) {
                    log.debug("关键词筛选：关键词为空，所有记录通过");
                    return true;
                }
                
                log.info("开始关键词匹配：keyword='{}'", request.getKeyword());
                boolean matchesKeyword = false;
                try {
                    // 搜索预约号
                    if (appt.getAppointmentNo() != null && 
                        appt.getAppointmentNo().toLowerCase().contains(request.getKeyword().toLowerCase())) {
                        matchesKeyword = true;
                        log.info("预约号匹配成功：预约 ID={}, 预约号={}", appt.getId(), appt.getAppointmentNo());
                    }
                    // 搜索会员名
                    if (!matchesKeyword && appt.getMemberId() != null) {
                        Member member = memberRepository.findById(appt.getMemberId()).orElse(null);
                        if (member != null && member.getRealName() != null) {
                            log.info("检查会员名：预约 ID={}, 会员 ID={}, 会员姓名='{}'", 
                                appt.getId(), appt.getMemberId(), member.getRealName());
                            if (member.getRealName().toLowerCase().contains(request.getKeyword().toLowerCase())) {
                                matchesKeyword = true;
                                log.info("会员名匹配成功：预约 ID={}, 会员姓名='{}'", appt.getId(), member.getRealName());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("关键词匹配失败：{}", e.getMessage());
                }
                log.info("关键词匹配结果：预约 ID={}, keyword='{}', 匹配成功={}", 
                    appt.getId(), request.getKeyword(), matchesKeyword);
                return matchesKeyword;
            })
            // 转换为响应对象
            .map(appt -> {
                AdminAppointmentResponse res = new AdminAppointmentResponse();
                BeanUtils.copyProperties(appt, res);
                
                // 设置状态描述
                res.setStatusDesc(getStatusDesc(appt.getStatus()));
                
                // 设置支付状态描述
                res.setPayStatusDesc(getPayStatusDesc(appt.getPayStatus()));
                
                // 查询会员信息
                try {
                    Member member = memberRepository.findById(appt.getMemberId()).orElse(null);
                    if (member != null) {
                        res.setMemberName(member.getRealName());
                        res.setMemberPhone(member.getPhone());
                    }
                } catch (Exception e) {
                    log.warn("查询会员信息失败 | memberId: {}", appt.getMemberId(), e);
                }
                
                // 查询教练信息
                try {
                    Coach coach = coachRepository.findById(appt.getCoachId()).orElse(null);
                    if (coach != null) {
                        res.setCoachName(coach.getRealName());
                    }
                } catch (Exception e) {
                    log.warn("查询教练信息失败 | coachId: {}", appt.getCoachId(), e);
                }
                
                // 查询门店信息
                try {
                    Store store = storeRepository.findById(appt.getStoreId()).orElse(null);
                    if (store != null) {
                        res.setStoreName(store.getStoreName());
                    }
                } catch (Exception e) {
                    log.warn("查询门店信息失败 | storeId: {}", appt.getStoreId(), e);
                }
                
                return res;
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmAppointment(AppointmentConfirmRequest request, Integer appointmentId) {
        log.info("========== 开始确认预约 | appointmentId: {} ==========", appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ResultCode.APPOINTMENT_NOT_FOUND));
        
        log.info("查询到的预约状态：{} ({}), 编号：{}", 
            appointment.getStatus(), getStatusDesc(appointment.getStatus()), appointment.getAppointmentNo());

        if (appointment.getStatus() != 0) {
            log.warn("预约状态不是待确认，无法确认 | appointmentId: {}, 当前状态：{}", appointmentId, appointment.getStatus());
            throw new BusinessException(ResultCode.APPOINTMENT_CONFIRM_INVALID);
        }

        log.info("开始更新预约状态为已确认...");
        appointment.setStatus(1); // 已确认
        appointment.setConfirmedAt(LocalDateTime.now());
        appointment.setConfirmedBy(2); // 管理员操作，实际应该从登录信息获取
        appointment.setRemark(request.getRemark());

        appointmentRepository.saveAndFlush(appointment); // 使用 saveAndFlush 确保立即保存到数据库
        
        log.info("预约确认成功 | appointmentId: {}, 新状态：{} ({}), 编号：{}", 
            appointmentId, appointment.getStatus(), getStatusDesc(appointment.getStatus()), appointment.getAppointmentNo());
        log.info("========== 预约确认完成 ==========");
    }

    @Override
    @Transactional
    public void completeAppointment(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ResultCode.APPOINTMENT_NOT_FOUND));

        if (appointment.getStatus() != 1) {
            throw new BusinessException(ResultCode.APPOINTMENT_COMPLETE_INVALID);
        }

        appointment.setStatus(2); // 已完成
        appointmentRepository.save(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminAppointmentResponse getAppointmentDetail(Integer appointmentId) {
        log.info("========== 查询预约详情 | appointmentId: {} ==========", appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ResultCode.APPOINTMENT_NOT_FOUND));
        
        log.info("数据库中的预约状态：{} ({}), 支付状态：{} ({})",
            appointment.getStatus(), getStatusDesc(appointment.getStatus()),
            appointment.getPayStatus(), getPayStatusDesc(appointment.getPayStatus()));
        
        AdminAppointmentResponse res = new AdminAppointmentResponse();
        BeanUtils.copyProperties(appointment, res);
        
        // 设置状态描述
        res.setStatusDesc(getStatusDesc(appointment.getStatus()));
        
        // 设置支付状态描述
        res.setPayStatusDesc(getPayStatusDesc(appointment.getPayStatus()));
        
        // 查询会员信息
        try {
            Member member = memberRepository.findById(appointment.getMemberId()).orElse(null);
            if (member != null) {
                res.setMemberName(member.getRealName());
                res.setMemberPhone(member.getPhone());
            }
        } catch (Exception e) {
            log.warn("查询会员信息失败 | memberId: {}", appointment.getMemberId(), e);
        }
        
        // 查询教练信息
        try {
            Coach coach = coachRepository.findById(appointment.getCoachId()).orElse(null);
            if (coach != null) {
                res.setCoachName(coach.getRealName());
            }
        } catch (Exception e) {
            log.warn("查询教练信息失败 | coachId: {}", appointment.getCoachId(), e);
        }
        
        // 查询门店信息
        try {
            Store store = storeRepository.findById(appointment.getStoreId()).orElse(null);
            if (store != null) {
                res.setStoreName(store.getStoreName());
            }
        } catch (Exception e) {
            log.warn("查询门店信息失败 | storeId: {}", appointment.getStoreId(), e);
        }
        
        log.info("查询预约详情成功 | appointmentNo: {}, 返回状态：{}", appointment.getAppointmentNo(), res.getStatus());
        return res;
    }

    @Override
    @Transactional
    public void markNoShow(Integer appointmentId, NoShowRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ResultCode.APPOINTMENT_NOT_FOUND));

        if (appointment.getStatus() != 1) {
            throw new BusinessException(ResultCode.APPOINTMENT_NOSHOW_INVALID);
        }

        appointment.setStatus(4); // 已爽约
        appointment.setCancelReason(request.getReason());
        appointment.setCancelBy(2); // 管理员操作
        appointmentRepository.save(appointment);
    }

    // 私有辅助方法
    private AppointmentResponse convertToResponse(Appointment appt) {
        AppointmentResponse res = new AppointmentResponse();
        BeanUtils.copyProperties(appt, res);
        
        // 设置状态描述
        res.setStatusDesc(getStatusDesc(appt.getStatus()));
        
        // 设置支付状态描述
        res.setPayStatusDesc(getPayStatusDesc(appt.getPayStatus()));
        
        // 关联查询教练姓名
        try {
            Coach coach = coachRepository.findById(appt.getCoachId()).orElse(null);
            if (coach != null) {
                res.setCoachName(coach.getRealName());
            }
        } catch (Exception e) {
            log.warn("查询教练信息失败 | coachId: {}", appt.getCoachId(), e);
        }
        
        // 关联查询门店名称
        try {
            Store store = storeRepository.findById(appt.getStoreId()).orElse(null);
            if (store != null) {
                res.setStoreName(store.getStoreName());
            }
        } catch (Exception e) {
            log.warn("查询门店信息失败 | storeId: {}", appt.getStoreId(), e);
        }
        
        return res;
    }

    // 辅助方法：获取状态描述
    private String getStatusDesc(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待确认";
            case 1 -> "已确认";
            case 2 -> "已完成";
            case 3 -> "已取消";
            case 4 -> "已爽约";
            default -> "未知";
        };
    }

    // 辅助方法：获取支付状态描述
    private String getPayStatusDesc(Integer payStatus) {
        if (payStatus == null) return "未知";
        return switch (payStatus) {
            case 0 -> "未支付";
            case 1 -> "已支付";
            default -> "未知";
        };
    }

    // ========== 订单相关方法 ==========

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(Integer memberId) {
        log.info("========== 查询会员订单列表 | memberId: {} ==========", memberId);
        
        List<Order> orders = orderRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        log.info("查询到 {} 条订单记录", orders.size());
        
        return orders.stream().map(order -> {
            OrderResponse response = new OrderResponse();
            BeanUtils.copyProperties(order, response);
            
            // 设置支付状态描述
            response.setPayStatusDesc(getPayStatusDesc(order.getPayStatus()));
            
            return response;
        }).collect(Collectors.toList());
    }
}