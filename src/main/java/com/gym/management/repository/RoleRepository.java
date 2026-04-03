package com.gym.management.repository;

import com.gym.management.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // 【重要】必须导入这个

import java.util.Optional;

@Repository // 【重要】加上这个注解，将其注册为 Spring Bean
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * 根据角色代码查找角色
     * 例如：传入 "ADMIN" 返回管理员角色对象
     */
    Optional<Role> findByRoleCode(String roleCode);
}