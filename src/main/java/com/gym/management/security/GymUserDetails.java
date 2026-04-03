package com.gym.management.security;

import com.gym.management.entity.Admin;
import com.gym.management.entity.Coach;
import com.gym.management.entity.Member;
import com.gym.management.entity.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 统一的 Spring Security UserDetails 实现
 * 用于包装 Admin, Coach, Member 三种不同类型的用户
 */
@Getter
public class GymUserDetails implements UserDetails {

    /**
     * 用户唯一标识 ID (无论哪种类型，都转为 Integer)
     */
    private final Integer userId;

    /**
     * 用户类型: ADMIN, COACH, MEMBER
     */
    private final String userType;

    /**
     * 登录账号 (username 或 phone)
     */
    private final String username;

    /**
     * 密码哈希
     */
    private final String password;

    /**
     * 角色列表 (Spring Security 权限控制用)
     */
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * 原始用户对象 (方便在业务中直接获取详细信息，如 realName, storeId 等)
     * 类型可能是 Admin, Coach, 或 Member
     */
    private final Object rawUser;

    /**
     * 账号是否未过期
     */
    private final boolean accountNonExpired;

    /**
     * 账号是否未锁定
     */
    private final boolean accountNonLocked;

    /**
     * 凭证(密码)是否未过期
     */
    private final boolean credentialsNonExpired;

    /**
     * 账号是否启用
     */
    private final boolean enabled;

    // ================= 构造函数 =================

    /**
     * 构造管理员用户详情
     */
    public static GymUserDetails ofAdmin(Admin admin, Role role) {
        List<GrantedAuthority> auths = new ArrayList<>();
        if (role != null && role.isActive()) {
            auths.add(new SimpleGrantedAuthority(role.getGrantedAuthority()));
        } else {
            // 默认角色
            auths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return new GymUserDetails(
                admin.getId(),
                "ADMIN",
                admin.getUsername(), // 管理员通常用 username 登录
                admin.getPasswordHash(),
                auths,
                admin,
                admin.getStatus() == 1 // status=1 为启用
        );
    }

    /**
     * 构造教练用户详情
     */
    public static GymUserDetails ofCoach(Coach coach, Role role) {
        List<GrantedAuthority> auths = new ArrayList<>();
        if (role != null && role.isActive()) {
            auths.add(new SimpleGrantedAuthority(role.getGrantedAuthority()));
        } else {
            auths.add(new SimpleGrantedAuthority("ROLE_COACH"));
        }

        return new GymUserDetails(
                coach.getId(),
                "COACH",
                coach.getPhone(), // 教练通常用 phone 登录
                coach.getPasswordHash(),
                auths,
                coach,
                coach.isActive() // 只有 isActive()=true 才允许登录
        );
    }

    /**
     * 构造会员用户详情
     */
    public static GymUserDetails ofMember(Member member, Role role) {
        List<GrantedAuthority> auths = new ArrayList<>();
        if (role != null && role.isActive()) {
            auths.add(new SimpleGrantedAuthority(role.getGrantedAuthority()));
        } else {
            auths.add(new SimpleGrantedAuthority("ROLE_MEMBER"));
        }

        // 会员登录校验：状态必须正常 (1) 且 未过期 (expireDate > today)
        boolean isEnabled = (member.getStatus() == 1) && !member.isExpired();

        return new GymUserDetails(
                member.getId(),
                "MEMBER",
                member.getPhone(), // 会员通常用 phone 登录
                member.getPasswordHash(),
                auths,
                member,
                isEnabled
        );
    }

    // 私有构造函数
    private GymUserDetails(Integer userId, String userType, String username, String password,
                           Collection<? extends GrantedAuthority> authorities, Object rawUser, boolean enabled) {
        this.userId = userId;
        this.userType = userType;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.rawUser = rawUser;
        this.enabled = enabled;
        // 默认账号和凭证都不过期，除非你有特殊业务逻辑
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
    }

    // ================= UserDetails 接口实现 =================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    // ================= 辅助方法 (方便 Controller 调用) =================

    /**
     * 获取真实姓名
     */
    public String getRealName() {
        if (rawUser instanceof Admin) return ((Admin) rawUser).getRealName();
        if (rawUser instanceof Coach) return ((Coach) rawUser).getRealName();
        if (rawUser instanceof Member) return ((Member) rawUser).getRealName();
        return "未知用户";
    }

    /**
     * 获取所属门店 ID (如果是管理员或教练)
     */
    public Integer getStoreId() {
        if (rawUser instanceof Admin) return ((Admin) rawUser).getStoreId();
        if (rawUser instanceof Coach) return ((Coach) rawUser).getStoreId();
        return null; // 会员可能没有固定门店，或者取 registerStoreId
    }
}