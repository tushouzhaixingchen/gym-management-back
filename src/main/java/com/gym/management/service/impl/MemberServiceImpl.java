package com.gym.management.service.impl;

import com.gym.management.dto.request.admin.*;
import com.gym.management.dto.request.member.ChangePasswordRequest;
import com.gym.management.dto.request.member.MemberProfileUpdateRequest;
import com.gym.management.dto.response.MemberProfileVO;
import com.gym.management.dto.response.MemberResponse;
import com.gym.management.dto.response.MemberVisitResponse;
import com.gym.management.entity.Member;
import com.gym.management.entity.MemberVisit;
import com.gym.management.entity.Store;
import com.gym.management.common.exception.BusinessException;
import com.gym.management.repository.MemberRepository;
import com.gym.management.repository.MemberVisitRepository;
import com.gym.management.repository.StoreRepository;
import com.gym.management.service.MemberService;
import com.gym.management.util.PasswordUtil; // 🔥 新增导入
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 🔥 建议加上日志
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j // 🔥 启用日志
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;
    private final MemberVisitRepository memberVisitRepository;

    // ❌ 删除 PasswordEncoder (既然用了自定义工具类，这里不需要 Spring 的 Encoder 了)
    // 除非你其他地方还在用，否则可以移除依赖

    // ================= 查询服务 =================
    // ... (queryMembers, getMemberDetail 保持不变) ...

    @Override
    public Page<MemberResponse> queryMembers(MemberQueryRequest request) {
        // ... 原有代码不变 ...
        int page = Math.max(0, request.getPage() - 1);
        int size = Math.min(request.getSize(), 100);
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<Member> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (request.getPhone() != null && !request.getPhone().isEmpty()) {
                predicates.add(cb.like(root.get("phone"), "%" + request.getPhone() + "%"));
            }
            if (request.getRealName() != null && !request.getRealName().isEmpty()) {
                predicates.add(cb.like(root.get("realName"), "%" + request.getRealName() + "%"));
            }
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }
            if (request.getCardType() != null) {
                predicates.add(cb.equal(root.get("cardType"), request.getCardType()));
            }
            if (request.getRegisterStoreId() != null) {
                predicates.add(cb.equal(root.get("registerStoreId"), request.getRegisterStoreId()));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<Member> pageData = memberRepository.findAll(spec, pageRequest);
        return pageData.map(member -> {
            String storeName = fetchStoreName(member.getRegisterStoreId());
            return MemberResponse.fromEntity(member, storeName);
        });
    }

    @Override
    public MemberResponse getMemberDetail(Integer id) {
        // ... 原有代码不变 ...
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException("会员不存在，ID: " + id));
        String storeName = fetchStoreName(member.getRegisterStoreId());
        return MemberResponse.fromEntity(member, storeName);
    }

    @Override
    public Member getMemberById(Integer id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException("会员不存在，ID: " + id));
    }

    // ================= 基础维护 =================

    @Override
    @Transactional
    public MemberResponse createMember(MemberCreateRequest request) {
        // 1. 检查手机号是否已存在
        if (memberRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("手机号已注册");
        }

        // 2. 生成随机明文密码
        String rawPassword = PasswordUtil.generateRandomPassword(); // 假设你有这个工具类

        // 3. 加密密码
        String passwordHash = PasswordUtil.encode(rawPassword);

        // 4. 构建实体
        Member member = Member.builder()
                .memberNo(generateMemberNo())
                .realName(request.getRealName())
                .phone(request.getPhone())
                .gender(request.getGender())
                .birthday(request.getBirthday())
                .email(request.getEmail())
                .registerStoreId(request.getRegisterStoreId())
                .cardType(request.getCardType())
                .totalTimes(request.getTotalTimes())
                .balance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO)
                .joinDate(LocalDate.now())
                .status(1)
                .passwordHash(passwordHash) // 存哈希
                .isInitialPassword(1)       // 标记为初始密码
                .build();

        // 5. 保存数据库
        memberRepository.save(member);

        // 6. 【关键】获取门店名称 (用于返回)
        String storeName = "";
        if (member.getRegisterStoreId() != null) {
            storeName = storeRepository.findById(member.getRegisterStoreId())
                    .map(Store::getStoreName)
                    .orElse("未知门店");
        }

        // 7. 转换为 Response
        MemberResponse response = MemberResponse.fromEntity(member, storeName);

        // 🔥🔥🔥 将明文密码填入响应对象 (仅此次返回有效)
        response.setTemporaryPassword(rawPassword);

        log.info("创建会员成功，ID: {}, 临时密码: {}", member.getId(), rawPassword);

        return response;
    }

    @Override
    @Transactional
    public MemberResponse updateMember(Integer id, MemberUpdateRequest request) {
        // ... existing code ...
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException("会员不存在"));

        if (request.getRealName() != null) member.setRealName(request.getRealName());

        // 只有当手机号确实发生改变时，才检查唯一性
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            // 检查新手机号是否与当前会员的手机号不同
            if (!request.getPhone().equals(member.getPhone())) {
                // 手机号已改变，检查是否被其他会员使用
                if (memberRepository.existsByPhone(request.getPhone())) {
                    throw new BusinessException("该手机号已被其他会员使用");
                }
                member.setPhone(request.getPhone());
            }
            // 如果手机号没变，不需要做任何操作
        }

        if (request.getEmail() != null) member.setEmail(request.getEmail());
        if (request.getRemark() != null) member.setRemark(request.getRemark());

        member.setUpdatedAt(LocalDateTime.now());

        Member saved = memberRepository.save(member);
        return MemberResponse.fromEntity(saved, fetchStoreName(saved.getRegisterStoreId()));
    }

    @Override
    @Transactional
    public MemberResponse resetMemberPassword(Integer id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException("会员不存在"));

        // 1. 生成新随机密码
        String rawPassword = PasswordUtil.generateRandomPassword();

        // 2. 加密并保存
        member.setPasswordHash(PasswordUtil.encode(rawPassword));
        member.setIsInitialPassword(1); // 强制下次修改
        member.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(member);

        // 3. 构建返回对象
        String storeName = fetchStoreName(member.getRegisterStoreId());
        MemberResponse response = MemberResponse.fromEntity(member, storeName);

        // 4. 🔥 关键：填入明文密码
        response.setTemporaryPassword(rawPassword);

        log.info("会员 {} 密码已重置，新密码: {}", member.getPhone(), rawPassword);
        return response;
    }

    @Override
    public MemberProfileVO getMemberProfile(Integer memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("会员不存在"));
        return convertToProfileVO(member);
    }

    @Override
    @Transactional
    public MemberProfileVO updateMemberProfile(Integer memberId, MemberProfileUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("会员不存在"));

        // 只允许会员修改自己的基础信息
        if (request.getRealName() != null) {
            member.setRealName(request.getRealName());
        }
        if (request.getGender() != null) {
            member.setGender(request.getGender());
        }
        if (request.getEmail() != null) {
            member.setEmail(request.getEmail());
        }
        if (request.getBirthday() != null) {
            member.setBirthday(request.getBirthday());
        }
        if (request.getRemark() != null) {
            member.setRemark(request.getRemark());
        }

        member.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(member);

        return convertToProfileVO(member);
    }

    @Override
    @Transactional
    public void changePassword(Integer memberId, ChangePasswordRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("会员不存在"));

        // 验证旧密码
        if (!PasswordUtil.matches(request.getOldPassword(), member.getPasswordHash())) {
            throw new BusinessException("旧密码错误");
        }

        // 验证新密码不能为空
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            throw new BusinessException("新密码不能为空");
        }

        // 更新密码
        member.setPasswordHash(PasswordUtil.encode(request.getNewPassword()));
        // 标记为非初始密码
        member.setIsInitialPassword(2);
        member.setUpdatedAt(LocalDateTime.now());

        memberRepository.save(member);
        log.info("会员 {} 密码已修改", member.getPhone());
    }

    /**
     * 将 Member 实体转换为 MemberProfileVO
     */
    private MemberProfileVO convertToProfileVO(Member member) {
        MemberProfileVO vo = new MemberProfileVO();
        vo.setId(member.getId());
        vo.setMemberNo(member.getMemberNo());
        vo.setRealName(member.getRealName());
        vo.setGender(member.getGender());
        vo.setGenderText(member.getGenderText());
        vo.setPhone(member.getPhone());
        vo.setEmail(member.getEmail());
        vo.setBirthday(member.getBirthday());
        vo.setRegisterStoreId(member.getRegisterStoreId());
        vo.setJoinDate(member.getJoinDate());
        vo.setExpireDate(member.getExpireDate());
        vo.setCardType(member.getCardType());
        vo.setTotalTimes(member.getTotalTimes());
        vo.setRemainingTimes(member.getRemainingTimes());
        vo.setBalance(member.getBalance());
        vo.setTotalConsumption(member.getTotalConsumption());
        vo.setVisitCount(member.getVisitCount());
        vo.setLastVisitAt(member.getLastVisitAt());
        vo.setLastVisitStoreId(member.getLastVisitStoreId());
        vo.setStatus(member.getStatus());
        vo.setStatusText(member.getStatusText());
        vo.setExpired(member.isExpired());
        vo.setRemark(member.getRemark());
        vo.setCreatedAt(member.getCreatedAt());
        vo.setUpdatedAt(member.getUpdatedAt());
        return vo;
    }

    @Override
    @Transactional
    public void deleteMember(Integer id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException("会员不存在"));

        // 检查会员是否已经是冻结状态
        if (member.getStatus() == 2) {
            throw new BusinessException("会员已是冻结状态");
        }

        // 将状态改为冻结（2）
        member.setStatus(2);
        member.setRemark((member.getRemark() == null ? "" : member.getRemark())
                + " [已冻结:" + LocalDateTime.now() + "]");
        member.setUpdatedAt(LocalDateTime.now());

        memberRepository.save(member);
    }


    // ================= 核心业务操作 =================
    // ... (openOrRenewCard, rechargeBalance, adjustTimes, updateMemberStatus 保持不变) ...

    @Override
    @Transactional
    public MemberResponse openOrRenewCard(Integer id, CardOperationRequest request) {
        // ... 原有逻辑不变 ...
        Member member = memberRepository.findById(id).orElseThrow(() -> new BusinessException("会员不存在"));
        String type = request.getCardType();
        LocalDate now = LocalDate.now();

        if ("period".equals(type) || "vip".equals(type)) {
            int months = "vip".equals(type) ? 120 : request.getDurationMonths();
            LocalDate newExpireDate;
            if (member.getExpireDate() != null && member.getExpireDate().isAfter(now) && member.getStatus() == 1) {
                newExpireDate = member.getExpireDate().plusMonths(months);
            } else {
                newExpireDate = now.plusMonths(months);
            }
            member.setCardType(type);
            member.setExpireDate(newExpireDate);
            member.setStatus(1);
        } else if ("times".equals(type)) {
            member.setCardType(type);
            if (member.getExpireDate() == null || member.getExpireDate().isBefore(now)) {
                member.setExpireDate(now.plusYears(1));
            }
            int addTimes = request.getTimesCount();
            member.setTotalTimes((member.getTotalTimes() == null ? 0 : member.getTotalTimes()) + addTimes);
            member.setRemainingTimes((member.getRemainingTimes() == null ? 0 : member.getRemainingTimes()) + addTimes);
            member.setStatus(1);
        } else {
            throw new BusinessException("不支持的卡类型：" + type);
        }
        member.setUpdatedAt(LocalDateTime.now());
        Member saved = memberRepository.save(member);
        return MemberResponse.fromEntity(saved, fetchStoreName(saved.getRegisterStoreId()));
    }

    @Override
    @Transactional
    public MemberResponse rechargeBalance(Integer id, BigDecimal amount, String remark) {
        // ... 原有逻辑不变 ...
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException("充值金额必须大于0");
        Member member = memberRepository.findById(id).orElseThrow(() -> new BusinessException("会员不存在"));
        member.setBalance(member.getBalance().add(amount));
        member.setUpdatedAt(LocalDateTime.now());
        Member saved = memberRepository.save(member);
        return MemberResponse.fromEntity(saved, fetchStoreName(saved.getRegisterStoreId()));
    }

    @Override
    @Transactional
    public MemberResponse adjustTimes(Integer id, Integer times, String reason) {
        // ... 原有逻辑不变 ...
        if (times == 0) return getMemberDetail(id);
        Member member = memberRepository.findById(id).orElseThrow(() -> new BusinessException("会员不存在"));
        if (!"times".equals(member.getCardType())) throw new BusinessException("只有次卡会员才能调整次数");
        int newRemaining = (member.getRemainingTimes() == null ? 0 : member.getRemainingTimes()) + times;
        if (newRemaining < 0) throw new BusinessException("调整后剩余次数不能为负数");
        member.setRemainingTimes(newRemaining);
        member.setUpdatedAt(LocalDateTime.now());
        Member saved = memberRepository.save(member);
        return MemberResponse.fromEntity(saved, fetchStoreName(saved.getRegisterStoreId()));
    }

    @Override
    @Transactional
    public MemberResponse updateMemberStatus(Integer id, MemberStatusRequest request) {
        // ... 原有逻辑不变 ...
        Member member = memberRepository.findById(id).orElseThrow(() -> new BusinessException("会员不存在"));
        member.setStatus(request.getStatus());
        member.setRemark((member.getRemark() == null ? "" : member.getRemark()) + " [状态变更:" + request.getReason() + "]");
        member.setUpdatedAt(LocalDateTime.now());
        Member saved = memberRepository.save(member);
        return MemberResponse.fromEntity(saved, fetchStoreName(saved.getRegisterStoreId()));
    }

    @Override
    @Transactional
    public MemberVisitResponse memberVisit(Integer memberId, MemberVisitRequest request) {
        // ... 原有逻辑不变 ...
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BusinessException("会员不存在，ID: " + memberId));
        if (request.getStoreId() != null) {
            storeRepository.findById(request.getStoreId()).orElseThrow(() -> new BusinessException("门店不存在，ID: " + request.getStoreId()));
        }
        if (member.getStatus() != 1) {
            String msg = (member.getStatus() == 0) ? "会员已过期" : "会员已被冻结";
            throw new BusinessException(msg + "，无法签到");
        }
        if (member.getExpireDate() != null && member.getExpireDate().isBefore(LocalDate.now())) {
            member.setStatus(0);
            memberRepository.save(member);
            throw new BusinessException("会员卡已过期，请续费后签到");
        }
        LocalDate today = LocalDate.now();
        boolean alreadyCheckedIn = memberVisitRepository.findTodayCheckIn(memberId, today).isPresent();
        if (alreadyCheckedIn) throw new BusinessException("今日已签到，请勿重复操作");

        int deductTimes = 0;
        if ("times".equals(member.getCardType())) {
            if (member.getRemainingTimes() == null || member.getRemainingTimes() <= 0) {
                throw new BusinessException("次卡剩余次数不足，无法签到");
            }
            member.setRemainingTimes(member.getRemainingTimes() - 1);
            deductTimes = 1;
        }

        member.setVisitCount((member.getVisitCount() == null ? 0 : member.getVisitCount()) + 1);
        member.setLastVisitAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(member);

        MemberVisit visit = new MemberVisit();
        visit.setMemberId(memberId);
        visit.setStoreId(request.getStoreId());
        visit.setCheckInTime(LocalDateTime.now());
        visit.setVisitDate(today);
        visit.setCheckInMethod(request.getCheckInMethod() != null ? request.getCheckInMethod() : "manual");
        visit.setCardType(member.getCardType());
        visit.setDeductTimes(deductTimes);
        visit.setRemark(request.getRemark());
        if (request.getRelatedAppointmentId() != null) visit.setRelatedAppointmentId(request.getRelatedAppointmentId());
        if (request.getRelatedCourseId() != null) visit.setRelatedCourseId(request.getRelatedCourseId());
        if (request.getStaffId() != null) visit.setStaffId(request.getStaffId());

        MemberVisit savedVisit = memberVisitRepository.save(visit);
        Long totalCheckIns = memberVisitRepository.countTotalCheckIns(memberId);

        Integer remainingDays = null;
        String expiryDateStr = null;
        if ("period".equals(member.getCardType()) || "vip".equals(member.getCardType())) {
            if (member.getExpireDate() != null) {
                remainingDays = (int) ChronoUnit.DAYS.between(today, member.getExpireDate());
                expiryDateStr = member.getExpireDate().toString();
            }
        }

        MemberVisitResponse response = new MemberVisitResponse();
        response.setSuccess(true);
        response.setMessage("签到成功");
        response.setId(savedVisit.getId());
        response.setMemberId(memberId);
        response.setMemberName(member.getRealName());
        response.setMemberNo(member.getMemberNo());
        response.setStoreId(request.getStoreId());
        response.setCheckInTime(savedVisit.getCheckInTime());
        response.setCheckInMethod(savedVisit.getCheckInMethod());
        response.setCardType(savedVisit.getCardType());
        response.setDeductTimes(deductTimes);
        response.setRemainingTimes(member.getRemainingTimes());
        response.setRemainingDays(remainingDays);
        response.setExpiryDate(expiryDateStr);
        response.setTotalCheckIns(totalCheckIns.intValue());

        return response;
    }

    // ================= 私有辅助方法 =================
    private String fetchStoreName(Integer storeId) {
        if (storeId == null) return "未知门店";
        return storeRepository.findById(storeId).map(Store::getStoreName).orElse("未知门店");
    }

    private String generateMemberNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        int random = (int)(Math.random() * 9000) + 1000;
        return "M" + dateStr + random;
    }
}