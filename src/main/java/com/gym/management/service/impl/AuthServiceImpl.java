package com.gym.management.service.impl;

import com.gym.management.common.ResultCode;
import com.gym.management.dto.request.admin.ChangePasswordRequest;
import com.gym.management.dto.request.common.LoginRequest;
import com.gym.management.dto.response.LoginResponse;
import com.gym.management.entity.Admin;
import com.gym.management.entity.Coach;
import com.gym.management.entity.Member;
import com.gym.management.common.exception.BusinessException;
import com.gym.management.repository.AdminRepository;
import com.gym.management.repository.CoachRepository;
import com.gym.management.repository.MemberRepository;
import com.gym.management.service.AuthService;
import com.gym.management.util.JwtUtil;
import com.gym.management.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AdminRepository adminRepository;
    private final CoachRepository coachRepository;
    private final MemberRepository memberRepository;

    private final JwtUtil jwtUtil;

    private static final String USER_TYPE_ADMIN = "ADMIN";
    private static final String USER_TYPE_COACH = "COACH";
    private static final String USER_TYPE_MEMBER = "MEMBER";

    @Override
    public LoginResponse login(LoginRequest request) {
        String account = request.getAccount();
        String password = request.getPassword();

        if (!StringUtils.hasText(account) || !StringUtils.hasText(password)) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }

        Object userObj = null;
        String userType = null;
        Integer userId = null;
        String realName = null;
        Integer roleId = null;
        Integer storeId = null;
        String dbPasswordHash = null;
        String loginAccount = null;

        // 🔥 初始化标志位：默认为 false (不需要改密)
        boolean isInitialPassword = false;

        // --- 1. 尝试查询管理员 ---
        Optional<Admin> adminOpt = adminRepository.findActiveByUsernameOrPhone(account, account);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            userObj = admin;
            userType = USER_TYPE_ADMIN;
            userId = admin.getId();
            realName = admin.getRealName();
            roleId = admin.getRoleId();
            storeId = admin.getStoreId();
            dbPasswordHash = admin.getPasswordHash();
            loginAccount = admin.getUsername();

            // ⛔️ 【关键修改】管理员不检查初始密码标志！
            // 即使数据库里 admin.getIsInitialPassword() == 1，我们也强制设为 false
            // 因为管理员密码由超管分配，无需强制用户自行修改
            isInitialPassword = false;

            log.debug("管理员登录成功: {}, 跳过初始密码检查", admin.getUsername());
        }

        // --- 2. 尝试查询教练 (保持原有逻辑) ---
        if (userObj == null) {
            Optional<Coach> coachOpt = coachRepository.findActiveByPhone(account);
            if (!coachOpt.isPresent()) {
                // 假设你有通过工号查询的方法，如果没有可以注释掉
                coachOpt = coachRepository.findActiveByCoachNo(account);
            }

            if (coachOpt.isPresent()) {
                Coach coach = coachOpt.get();
                userObj = coach;
                userType = USER_TYPE_COACH;
                userId = coach.getId();
                realName = coach.getRealName();
                roleId = coach.getRoleId();
                storeId = coach.getStoreId();
                dbPasswordHash = coach.getPasswordHash();
                loginAccount = coach.getPhone();

                // ✅ 教练需要检查初始密码
                if (coach.getIsInitialPassword() != null && coach.getIsInitialPassword() == 1) {
                    isInitialPassword = true;
                }
            }
        }

        // --- 3. 尝试查询会员 (保持原有逻辑) ---
        if (userObj == null) {
            Optional<Member> memberOpt = memberRepository.findActiveByPhoneOrMemberNo(account, account);
            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();
                userObj = member;
                userType = USER_TYPE_MEMBER;
                userId = member.getId();
                realName = member.getRealName();
                roleId = member.getRoleId();
                storeId = null;
                dbPasswordHash = member.getPasswordHash();
                loginAccount = member.getPhone();

                // ✅ 会员需要检查初始密码
                if (member.getIsInitialPassword() != null && member.getIsInitialPassword() == 1) {
                    isInitialPassword = true;
                }
            }
        }

        // --- 4. 校验用户是否存在 ---
        if (userObj == null) {
            // 统一提示，防止枚举攻击
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        // --- 5. 校验密码 ---
        if (!PasswordUtil.matches(password, dbPasswordHash)) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        // --- 6. 生成 Token ---
        String token = jwtUtil.generateToken(userId, loginAccount, userType);

        // --- 7. 更新最后登录时间 (异步或同步) ---
        updateLastLogin(userObj, userType);

        // --- 8. 构建响应 ---
        return LoginResponse.builder()
                .token(token)
                // 🔥 只有当 isInitialPassword 为 true 时 (即会员或教练的初始状态)，前端才强制改密
                // 管理员永远是 false
                .forceChangePassword(isInitialPassword)
                .userInfo(LoginResponse.UserInfoVO.builder()
                        .id(userId)
                        .account(loginAccount)
                        .realName(realName)
                        .userType(userType)
                        .roleId(roleId)
                        .storeId(storeId)
                        .build())
                .build();
    }

    @Override
    public LoginResponse getCurrentUser() {
        // TODO: 后续结合 SecurityContext 实现
        throw new BusinessException(500, "获取当前用户信息功能待实现");
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // TODO: 后续结合 SecurityContext 实现
        // 注意：此方法通常只允许会员和教练调用，管理员应禁止调用或只能由超管重置
        throw new BusinessException(500, "修改密码功能待实现");
    }

    @Override
    public void logout(String token) {
        log.info("用户登出，Token: {}", token);
        // TODO: Redis 黑名单逻辑
    }

    /**
     * 辅助方法：更新最后登录时间
     */
    private void updateLastLogin(Object userObj, String userType) {
        LocalDateTime now = LocalDateTime.now();
        try {
            if (USER_TYPE_ADMIN.equals(userType) && userObj instanceof Admin) {
                Admin admin = (Admin) userObj;
                admin.setLastLoginAt(now);
                adminRepository.save(admin);
            }
//            else if (USER_TYPE_COACH.equals(userType) && userObj instanceof Coach) {
//                Coach coach = (Coach) userObj;
//                coach.setLastLoginAt(now);
//                coachRepository.save(coach);
//            } else if (USER_TYPE_MEMBER.equals(userType) && userObj instanceof Member) {
//                Member member = (Member) userObj;
//                member.setLastLoginAt(now);
//                memberRepository.save(member);
//            }
        } catch (Exception e) {
            log.warn("更新最后登录时间失败", e);
            // 登录成功即可，更新最后登录时间失败不应阻断流程
        }
    }
}