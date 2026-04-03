package com.gym.management.service;

import com.gym.management.entity.Admin;
import com.gym.management.entity.Coach;
import com.gym.management.entity.Member;
import com.gym.management.entity.Role;
import com.gym.management.repository.AdminRepository;
import com.gym.management.repository.CoachRepository;
import com.gym.management.repository.MemberRepository;
import com.gym.management.repository.RoleRepository;
import com.gym.management.security.GymUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GymUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final CoachRepository coachRepository;
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginInput) throws UsernameNotFoundException {
        log.info("尝试登录，账号/手机号: {}", loginInput);

        // 1. 尝试在管理员表中查找 (匹配 username 或 phone)
        Optional<Admin> adminOpt = adminRepository.findByUsernameOrPhone(loginInput, loginInput);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            Role role = roleRepository.findById(admin.getRoleId())
                    .orElse(null); // 如果角色被删了，给个 null 后面处理
            log.info("找到管理员用户: {}", admin.getUsername());
            return GymUserDetails.ofAdmin(admin, role);
        }

        // 2. 尝试在教练表中查找 (匹配 phone)
        Optional<Coach> coachOpt = coachRepository.findByPhone(loginInput);
        if (coachOpt.isPresent()) {
            Coach coach = coachOpt.get();
            Role role = roleRepository.findById(coach.getRoleId())
                    .orElse(null);
            log.info("找到教练用户: {}", coach.getPhone());
            return GymUserDetails.ofCoach(coach, role);
        }

        // 3. 尝试在会员表中查找 (匹配 phone 或 memberNo)
        // 注意：会员通常用手机号登录，但也可能允许用会员卡号登录
        Optional<Member> memberOpt = memberRepository.findByPhoneOrMemberNo(loginInput, loginInput);
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            Role role = roleRepository.findById(member.getRoleId())
                    .orElse(null);
            log.info("找到会员用户: {}", member.getPhone());
            return GymUserDetails.ofMember(member, role);
        }

        // 如果都没找到
        log.warn("未找到用户: {}", loginInput);
        throw new UsernameNotFoundException("用户不存在或账号密码错误: " + loginInput);
    }
}