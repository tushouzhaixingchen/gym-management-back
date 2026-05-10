package com.gym.management.service.impl;

import com.gym.management.dto.request.admin.*;
import com.gym.management.dto.request.member.CourseBookingRequest;
import com.gym.management.dto.request.member.PaymentRequest;
import com.gym.management.dto.response.CourseResponse;
import com.gym.management.dto.response.CourseBookResponse;
import com.gym.management.dto.response.CourseBookingResponse;
import com.gym.management.entity.Course;
import com.gym.management.entity.Store;
import com.gym.management.entity.Coach;
import com.gym.management.entity.Admin;
import com.gym.management.entity.CourseBooking;
import com.gym.management.common.exception.BusinessException;
import com.gym.management.repository.CourseRepository;
import com.gym.management.repository.StoreRepository;
import com.gym.management.repository.CoachRepository;
import com.gym.management.repository.AdminRepository;
import com.gym.management.repository.CourseBookingRepository;
import com.gym.management.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final StoreRepository storeRepository;
    private final CoachRepository coachRepository;
    private final AdminRepository adminRepository;
    private final CourseBookingRepository courseBookingRepository;

    // ================= 辅助方法：获取当前登录管理员信息 =================

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

    /**
     * 获取当前登录管理员的信息
     * @return Admin 对象，如果未找到返回 null
     */
    private Admin getCurrentAdmin() {
        Integer adminId = getCurrentAdminId();
        if (adminId == null) return null;
        return adminRepository.findById(adminId).orElse(null);
    }

    /**
     * 检查是否为超级管理员（roleId=1）
     */
    private boolean isSuperAdmin(Admin admin) {
        if (admin == null) return false;
        return admin.getRoleId() != null && admin.getRoleId() == 1;
    }

    // ================= 课程管理 =================

    @Override
    public Page<CourseResponse> queryCourses(CourseQueryRequest request) {
        // 🔴 权限检查：获取当前登录管理员信息
        Admin currentAdmin = getCurrentAdmin();
        boolean isSuperAdmin = isSuperAdmin(currentAdmin);
        Integer currentStoreId = currentAdmin != null ? currentAdmin.getStoreId() : null;
        
        log.info("【权限检查】课程列表查询 - 管理员 ID: {}, 用户名：{}, 角色 ID: {}, 是否超级管理员：{}", 
            currentAdmin != null ? currentAdmin.getId() : "null",
            currentAdmin != null ? currentAdmin.getUsername() : "null",
            currentAdmin != null ? currentAdmin.getRoleId() : "null",
            isSuperAdmin);
        
        // 确定最终使用的门店 ID
        // 规则：
        // 1. 超级管理员：可以使用前端传的 storeId，也可以不传（查询所有）
        // 2. 普通管理员：只能查询本店，忽略前端传的 storeId，强制使用当前管理员的 storeId
        final Integer effectiveStoreId;  // 🔴 声明为 final，以便在 lambda 中使用
        
        if (isSuperAdmin) {
            effectiveStoreId = request.getStoreId();
            if (effectiveStoreId == null) {
                log.info("【权限检查】✓ 超级管理员模式：查询所有门店的课程");
            } else {
                log.info("【权限检查】✓ 超级管理员模式：查询指定门店 ID={} 的课程", effectiveStoreId);
            }
        } else if (currentStoreId != null) {
            effectiveStoreId = currentStoreId;
            log.info("【权限检查】✓ 门店管理员模式：只能查询本店 ID={} 的课程", effectiveStoreId);
        } else {
            effectiveStoreId = null;
            log.warn("【权限检查】✗ 当前用户既不是超级管理员也没有门店 ID，无权访问");
        }
        
        int page = Math.max(0, request.getPage() - 1);
        int size = Math.min(request.getSize(), 100);
        Sort sort = Sort.by(Sort.Direction.ASC, "startTime");
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<Course> spec = (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();

            // 🔴 添加门店权限过滤
            if (effectiveStoreId != null) {
                predicates.add(cb.equal(root.get("storeId"), effectiveStoreId));
                log.debug("【权限过滤】添加门店条件：storeId={}", effectiveStoreId);
            }
            
            if (request.getCoachId() != null) {
                predicates.add(cb.equal(root.get("coachId"), request.getCoachId()));
            }
            if (request.getCourseName() != null && !request.getCourseName().isEmpty()) {
                predicates.add(cb.like(root.get("courseName"), "%" + request.getCourseName() + "%"));
            }
            if (request.getCourseType() != null && !request.getCourseType().isEmpty()) {
                predicates.add(cb.equal(root.get("courseType"), request.getCourseType()));
            }
            if (request.getCourseLevel() != null && !request.getCourseLevel().isEmpty()) {
                predicates.add(cb.equal(root.get("courseLevel"), request.getCourseLevel()));
            }
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }
            if (request.getStartTimeStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), request.getStartTimeStart()));
            }
            if (request.getStartTimeEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startTime"), request.getStartTimeEnd()));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<Course> pageData = courseRepository.findAll(spec, pageRequest);
        return pageData.map(course -> {
            String storeName = fetchStoreName(course.getStoreId());
            String coachName = fetchCoachName(course.getCoachId());
            return CourseResponse.fromEntity(course, storeName, coachName);
        });
    }

    @Override
    public CourseResponse getCourseDetail(Integer id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("课程不存在，ID: " + id));
        String storeName = fetchStoreName(course.getStoreId());
        String coachName = fetchCoachName(course.getCoachId());
        return CourseResponse.fromEntity(course, storeName, coachName);
    }

    @Override
    @Transactional
    public CourseResponse createCourse(CourseCreateRequest request) {
        // 验证时间
        if (request.getEndTime().isBefore(request.getStartTime()) ||
                request.getEndTime().isEqual(request.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }

        // 计算课程时长
        int durationMinutes = (int) java.time.Duration
                .between(request.getStartTime(), request.getEndTime())
                .toMinutes();

        // 检查教练同一时间段是否有其他课程
        checkCoachScheduleConflict(request.getCoachId(), request.getStartTime(), request.getEndTime());

        // 检查教室同一时间段是否有其他课程
        if (request.getRoom() != null) {
            checkRoomScheduleConflict(request.getStoreId(), request.getRoom(),
                    request.getStartTime(), request.getEndTime());
        }

        Course course = Course.builder()
                .storeId(request.getStoreId())
                .coachId(request.getCoachId())
                .courseName(request.getCourseName())
                .courseType(request.getCourseType())
                .courseLevel(request.getCourseLevel() != null ? request.getCourseLevel() : "beginner")
                .maxSeats(request.getMaxSeats() != null ? request.getMaxSeats() : 20)
                .bookedSeats(0)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .durationMinutes(durationMinutes)
                .price(request.getPrice() != null ? request.getPrice() : java.math.BigDecimal.ZERO)
                .room(request.getRoom())
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .remark(request.getRemark())
                .build();

        courseRepository.save(course);

        String storeName = fetchStoreName(course.getStoreId());
        String coachName = fetchCoachName(course.getCoachId());
        log.info("创建课程成功，ID: {}, 课程名称：{}", course.getId(), course.getCourseName());

        return CourseResponse.fromEntity(course, storeName, coachName);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Integer id, CourseUpdateRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("课程不存在"));

        // 已开始的课程不能修改
        if (course.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("课程已开始，无法修改");
        }

        if (request.getCourseName() != null) course.setCourseName(request.getCourseName());
        if (request.getCourseType() != null) course.setCourseType(request.getCourseType());
        if (request.getCourseLevel() != null) course.setCourseLevel(request.getCourseLevel());
        if (request.getMaxSeats() != null) course.setMaxSeats(request.getMaxSeats());
        if (request.getStartTime() != null) course.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) course.setEndTime(request.getEndTime());
        if (request.getDurationMinutes() != null) course.setDurationMinutes(request.getDurationMinutes());
        if (request.getPrice() != null) course.setPrice(request.getPrice());
        if (request.getRoom() != null) course.setRoom(request.getRoom());
        if (request.getStatus() != null) course.setStatus(request.getStatus());
        if (request.getRemark() != null) course.setRemark(request.getRemark());
        if (request.getCoachId() != null) course.setCoachId(request.getCoachId());

        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        String storeName = fetchStoreName(course.getStoreId());
        String coachName = fetchCoachName(course.getCoachId());
        return CourseResponse.fromEntity(course, storeName, coachName);
    }

    @Override
    @Transactional
    public void deleteCourse(Integer id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("课程不存在"));

        // 已开始的课程不能删除
        if (course.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("课程已开始，无法删除");
        }

        // 有预约记录的课程不能删除
        if (course.getBookedSeats() > 0) {
            throw new BusinessException("课程已有预约记录，无法删除");
        }

        // 软删除：将状态改为取消
        course.setStatus(2);
        course.setRemark((course.getRemark() == null ? "" : course.getRemark())
                + " [已取消:" + LocalDateTime.now() + "]");
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);
    }

    // ================= 课程预约管理 =================

    @Override
    @Transactional
    public CourseBookResponse bookCourse(CourseBookRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new BusinessException("课程不存在"));

        // 检查课程状态
        if (course.getStatus() != 1) {
            throw new BusinessException("课程不可预约，当前状态：" + CourseResponse.getStatusText(course.getStatus()));
        }

        // 检查是否已满
        if (course.getBookedSeats() >= course.getMaxSeats()) {
            throw new BusinessException("课程已满，无法预约");
        }

        // 检查时间
        if (course.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("课程已开始，无法预约");
        }

        // TODO: 检查会员是否已预约该课程
        // TODO: 创建预约记录

        // 更新已预约人数
        course.setBookedSeats(course.getBookedSeats() + 1);

        // 如果已满，更新课程状态
        if (course.getBookedSeats() >= course.getMaxSeats()) {
            course.setStatus(0);
        }

        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        String storeName = fetchStoreName(course.getStoreId());
        String coachName = fetchCoachName(course.getCoachId());
        CourseResponse courseResponse = CourseResponse.fromEntity(course, storeName, coachName);

        return CourseBookResponse.builder()
                .courseId(course.getId())
                .courseName(course.getCourseName())
                .memberId(request.getMemberId())
                .status(1)
                .statusText("已预约")
                .bookTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public CourseBookResponse cancelBook(CourseCancelBookRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new BusinessException("课程不存在"));

        // 检查课程状态
        if (course.getStatus() == 2) {
            throw new BusinessException("课程已取消");
        }

        if (course.getStatus() == 4) {
            throw new BusinessException("课程已结束，无法取消预约");
        }

        // TODO: 检查会员是否有预约记录
        // TODO: 更新预约记录状态

        // 减少已预约人数
        if (course.getBookedSeats() > 0) {
            course.setBookedSeats(course.getBookedSeats() - 1);
        }

        // 如果之前是已满状态，恢复为可预约
        if (course.getStatus() == 0) {
            course.setStatus(1);
        }

        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        return CourseBookResponse.builder()
                .courseId(course.getId())
                .courseName(course.getCourseName())
                .memberId(request.getMemberId())
                .status(2)
                .statusText("已取消")
                .cancelTime(LocalDateTime.now())
                .cancelReason(request.getCancelReason())
                .build();
    }

    @Override
    public Page<CourseBookResponse> queryCourseBooks(Integer courseId, Integer page, Integer size) {
        // TODO: 实现预约记录查询
        int pageNum = Math.max(0, page - 1);
        int pageSize = Math.min(size != null ? size : 10, 100);
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "bookTime"));

        // 这里需要 CourseBookRepository 支持，暂时返回空页面
        return Page.empty(pageRequest);
    }

    // ================= 课程状态管理 =================

    @Override
    @Transactional
    public CourseResponse startCourse(Integer id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("课程不存在"));

        if (course.getStatus() != 1) {
            throw new BusinessException("只有可预约状态的课程才能开始");
        }

        if (course.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException("课程开始时间未到，无法开始");
        }

        course.setStatus(3); // 进行中
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        String storeName = fetchStoreName(course.getStoreId());
        String coachName = fetchCoachName(course.getCoachId());
        return CourseResponse.fromEntity(course, storeName, coachName);
    }

    @Override
    @Transactional
    public CourseResponse finishCourse(Integer id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("课程不存在"));

        if (course.getStatus() != 3) {
            throw new BusinessException("只有进行中的课程才能结束");
        }

        course.setStatus(4); // 已结束
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        String storeName = fetchStoreName(course.getStoreId());
        String coachName = fetchCoachName(course.getCoachId());
        return CourseResponse.fromEntity(course, storeName, coachName);
    }

    @Override
    @Transactional
    public CourseResponse cancelCourse(Integer id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("课程不存在"));

        if (course.getStatus() == 3) {
            throw new BusinessException("进行中的课程不能取消");
        }

        if (course.getStatus() == 4) {
            throw new BusinessException("已结束的课程不能取消");
        }

        course.setStatus(2); // 已取消
        course.setBookedSeats(0); // 清空预约
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        String storeName = fetchStoreName(course.getStoreId());
        String coachName = fetchCoachName(course.getCoachId());
        return CourseResponse.fromEntity(course, storeName, coachName);
    }

    // ================= 统计 =================

    @Override
    public int getAvailableSeats(Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException("课程不存在"));
        return course.getMaxSeats() - course.getBookedSeats();
    }

    @Override
    public boolean isCourseFull(Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException("课程不存在"));
        return course.getBookedSeats() >= course.getMaxSeats();
    }

    // ================= 私有辅助方法 =================

    private String fetchStoreName(Integer storeId) {
        if (storeId == null) return "未知门店";
        return storeRepository.findById(storeId)
                .map(Store::getStoreName)
                .orElse("未知门店");
    }

    private String fetchCoachName(Integer coachId) {
        if (coachId == null) return "未分配教练";
        return coachRepository.findById(coachId)
                .map(Coach::getRealName)
                .orElse("未知教练");
    }

    private void checkCoachScheduleConflict(Integer coachId, LocalDateTime start, LocalDateTime end) {
        if (coachId == null) return;

        List<Course> conflicts = courseRepository.findByStoreIdAndTimeRange(
                        null, start, end).stream()
                .filter(c -> c.getCoachId() != null && c.getCoachId().equals(coachId))
                .filter(c -> c.getStatus() != 2) // 排除已取消的课程
                .toList();

        if (!conflicts.isEmpty()) {
            throw new BusinessException("教练在该时间段已有其他课程安排");
        }
    }

    private void checkRoomScheduleConflict(Integer storeId, String room, LocalDateTime start, LocalDateTime end) {
        if (room == null || storeId == null) return;

        List<Course> conflicts = courseRepository.findByStoreIdAndTimeRange(
                        storeId, start, end).stream()
                .filter(c -> c.getRoom() != null && c.getRoom().equals(room))
                .filter(c -> c.getStatus() != 2) // 排除已取消的课程
                .toList();

        if (!conflicts.isEmpty()) {
            throw new BusinessException("该教室在该时间段已被占用");
        }
    }

    // ================= 会员端课程报名实现 =================

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> queryCoursesForMember(Integer page, Integer size, Integer storeId) {
        log.info("========== 查询可报名课程列表 | page: {}, size: {}, storeId: {} ==========", page, size, storeId);
        
        // 分页参数处理
        int pageNum = Math.max(0, page - 1);
        int pageSize = Math.min(size, 100);
        Sort sort = Sort.by(Sort.Direction.ASC, "startTime");
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize, sort);

        // 构建查询条件：只查询可预约的课程（status=1）
        Specification<Course> spec = (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            
            // 只查询可预约的课程
            predicates.add(cb.equal(root.get("status"), 1));
            
            // 如果指定了门店，则过滤
            if (storeId != null) {
                predicates.add(cb.equal(root.get("storeId"), storeId));
            }
            
            // 只查询未开始的课程
            predicates.add(cb.greaterThan(root.get("startTime"), LocalDateTime.now()));
            
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<Course> pageData = courseRepository.findAll(spec, pageRequest);
        log.info("查询到 {} 条可报名课程", pageData.getTotalElements());
        
        return pageData.map(course -> {
            String storeName = fetchStoreName(course.getStoreId());
            String coachName = fetchCoachName(course.getCoachId());
            return CourseResponse.fromEntity(course, storeName, coachName);
        });
    }

    @Override
    @Transactional
    public CourseBookingResponse memberBookCourse(CourseBookingRequest request, Integer memberId) {
        log.info("========== 会员报名课程 | memberId: {}, courseId: {} ==========", memberId, request.getCourseId());

        // 1. 查询课程信息
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new BusinessException("课程不存在"));

        // 2. 检查课程状态
        if (course.getStatus() == 0) {
            throw new BusinessException("课程已满，无法报名");
        }
        if (course.getStatus() == 2) {
            throw new BusinessException("课程已取消");
        }
        if (course.getStatus() == 3) {
            throw new BusinessException("课程进行中，无法报名");
        }
        if (course.getStatus() == 4) {
            throw new BusinessException("课程已结束，无法报名");
        }

        // 3. 检查是否已报名
        boolean alreadyBooked = courseBookingRepository.existsByMemberIdAndCourseId(memberId, request.getCourseId());
        if (alreadyBooked) {
            throw new BusinessException("您已报名该课程");
        }

        // 4. 检查剩余座位
        int availableSeats = course.getMaxSeats() - course.getBookedSeats();
        if (availableSeats <= 0) {
            throw new BusinessException("课程已满，无法报名");
        }

        // 5. 创建报名记录（直接设置为已支付）
        String bookingNo = "CB" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", memberId);

        CourseBooking booking = CourseBooking.builder()
                .storeId(course.getStoreId())
                .bookingNo(bookingNo)
                .memberId(memberId)
                .courseId(course.getId())
                .price(course.getPrice())
                .coachShare(java.math.BigDecimal.ZERO)
                .status(0)
                .payStatus(1)
                .payMethod(request.getPayMethod())
                .payTime(LocalDateTime.now())
                .remark(request.getRemark())
                .build();

        courseBookingRepository.save(booking);
        log.info("创建课程报名记录成功 | bookingNo: {}, courseId: {}, memberId: {}", 
                bookingNo, course.getId(), memberId);

        // 6. 【关键】报名成功后，课程的 booked_seats +1
        course.setBookedSeats(course.getBookedSeats() + 1);
        
        // 7. 如果报名后已满，更新课程状态为已满
        if (course.getBookedSeats() >= course.getMaxSeats()) {
            course.setStatus(0);
            log.info("课程已满，更新状态 | courseId: {}, bookedSeats: {}/{}", 
                    course.getId(), course.getBookedSeats(), course.getMaxSeats());
        }
        
        courseRepository.save(course);
        log.info("报名成功，课程报名人数+1 | courseId: {}, 当前报名人数: {}/{}", 
                course.getId(), course.getBookedSeats(), course.getMaxSeats());

        // 8. 构建响应
        return convertToBookingResponse(booking, course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseBookingResponse> getMyCourseBookings(Integer memberId) {
        log.info("========== 查询会员课程报名列表 | memberId: {} ==========", memberId);
        
        List<CourseBooking> bookings = courseBookingRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        log.info("查询到 {} 条报名记录", bookings.size());

        return bookings.stream()
                .map(booking -> {
                    Course course = courseRepository.findById(booking.getCourseId()).orElse(null);
                    return convertToBookingResponse(booking, course);
                })
                .toList();
    }

    @Override
    @Transactional
    public CourseBookingResponse payCourseBooking(Integer bookingId, PaymentRequest request) {
        log.info("========== 支付课程报名 | bookingId: {} ==========", bookingId);

        // 1. 查询报名记录
        CourseBooking booking = courseBookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("报名记录不存在"));

        // 2. 校验状态
        if (booking.getPayStatus() == 1) {
            throw new BusinessException("该报名已支付");
        }

        // 3. 查询课程信息
        Course course = courseRepository.findById(booking.getCourseId())
                .orElseThrow(() -> new BusinessException("课程不存在"));

        // 4. 更新支付信息
        booking.setPayStatus(1);
        booking.setPayMethod(request.getPayMethod());
        booking.setPayTime(LocalDateTime.now());
        courseBookingRepository.save(booking);

        // 5. 【关键】支付成功后，课程的 booked_seats +1
        course.setBookedSeats(course.getBookedSeats() + 1);
        
        // 6. 如果报名后已满，更新课程状态为已满
        if (course.getBookedSeats() >= course.getMaxSeats()) {
            course.setStatus(0); // 0 表示已满
            log.info("课程已满，更新状态 | courseId: {}, bookedSeats: {}/{}", 
                    course.getId(), course.getBookedSeats(), course.getMaxSeats());
        }
        
        courseRepository.save(course);
        log.info("支付成功，课程报名人数+1 | courseId: {}, 当前报名人数: {}/{}", 
                course.getId(), course.getBookedSeats(), course.getMaxSeats());

        // 7. 构建响应
        return convertToBookingResponse(booking, course);
    }

    /**
     * 将 CourseBooking 实体转换为响应对象
     */
    private CourseBookingResponse convertToBookingResponse(CourseBooking booking, Course course) {
        CourseBookingResponse response = CourseBookingResponse.builder()
                .id(booking.getId())
                .bookingNo(booking.getBookingNo())
                .courseId(booking.getCourseId())
                .price(booking.getPrice())
                .status(booking.getStatus())
                .statusText(CourseBookingResponse.getStatusText(booking.getStatus()))
                .payStatus(booking.getPayStatus())
                .payStatusText(CourseBookingResponse.getPayStatusText(booking.getPayStatus()))
                .payTime(booking.getPayTime())
                .payMethod(booking.getPayMethod())
                .payMethodText(CourseBookingResponse.getPayMethodText(booking.getPayMethod()))
                .memberCheckInTime(booking.getMemberCheckInTime())
                .memberCheckOutTime(booking.getMemberCheckOutTime())
                .feedbackScore(booking.getFeedbackScore())
                .remark(booking.getRemark())
                .createdAt(booking.getCreatedAt())
                .build();

        if (course != null) {
            response.setCourseName(course.getCourseName());
            response.setCourseType(course.getCourseType());
            response.setStartTime(course.getStartTime());
            response.setEndTime(course.getEndTime());
            response.setRoom(course.getRoom());
            response.setStoreId(course.getStoreId());
            response.setCoachId(course.getCoachId());
            
            // 获取门店名称
            String storeName = fetchStoreName(course.getStoreId());
            response.setStoreName(storeName);
            
            // 获取教练名称
            String coachName = fetchCoachName(course.getCoachId());
            response.setCoachName(coachName);
        }

        return response;
    }
}