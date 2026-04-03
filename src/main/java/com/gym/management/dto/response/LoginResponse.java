package com.gym.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    /**
     * JWT Token
     */
    private String token;

    /**
     * 🔥 新增字段：是否强制修改密码
     * true: 表示是初始密码，前端需拦截并跳转至改密页
     * false: 表示密码已修改过，正常进入系统
     */
    @Builder.Default
    private Boolean forceChangePassword = false;

    /**
     * 用户核心信息（统一封装，适配所有角色）
     */
    private UserInfoVO userInfo;

    /**
     * 权限列表 (用于前端按钮级控制)
     */
    private List<String> permissions;

    /**
     * 内部静态类：用户信息视图对象
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfoVO {
        /**
         * 用户ID
         */
        private Integer id;

        /**
         * 账号
         */
        private String account;

        /**
         * 真实姓名
         */
        private String realName;

        /**
         * 用户类型标识 (ADMIN, COACH, MEMBER)
         */
        private String userType;

        /**
         * 角色ID
         */
        private Integer roleId;

        /**
         * 所属门店ID
         */
        private Integer storeId;

        /**
         * 头像URL
         */
        private String avatar;

        // 💡 可选优化：你也可以直接把 isInitialPassword 放在这里，
        // 但放在外层更符合“登录动作的即时状态”语义，推荐放外层。
    }
}