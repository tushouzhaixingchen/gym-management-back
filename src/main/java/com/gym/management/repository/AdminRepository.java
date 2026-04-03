package com.gym.management.repository;

import com.gym.management.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {

    // ================= 【新增/优化】用于登录的核心方法 =================

    /**
     * 🔥 推荐：根据用户名 或 手机号 查找【状态正常】的管理员
     * 只有 status >= 0 的用户才能被查到，用于登录验证
     * 注意：这里假设 status >= 0 为正常状态，请根据你的实体定义确认
     */
    @Query("SELECT a FROM Admin a WHERE " +
            "(a.username = :username OR a.phone = :phone) AND " +
            "a.status >= 0")
    Optional<Admin> findActiveByUsernameOrPhone(
            @Param("username") String username,
            @Param("phone") String phone
    );

    /**
     * 原有方法保留（用于非登录场景，如后台需要查看已禁用账号详情）
     */
    Optional<Admin> findByUsernameOrPhone(String username, String phone);

    /**
     * 检查手机号是否已存在
     */
    boolean existsByPhone(String phone);

    // ================= 原有方法 (保留) =================

    Optional<Admin> findByUsername(String username);

    boolean existsByUsername(String username);

    // 根据用户名查询（排除已删除的/状态异常的）- 原有保留
    @Query("SELECT a FROM Admin a WHERE a.username = :username AND a.status >= 0")
    Optional<Admin> findByUsernameAndStatusNormal(@Param("username") String username);

    // 根据 ID 查询（排除已删除的）- 原有保留
    @Query("SELECT a FROM Admin a WHERE a.id = :id AND a.status >= 0")
    Optional<Admin> findByIdAndStatusNormal(@Param("id") Integer id);

    // 模糊搜索（用户名、姓名、手机）- 原有保留
    @Query("SELECT a FROM Admin a WHERE a.status >= 0 " +
            "AND (a.username LIKE %:keyword% OR a.realName LIKE %:keyword% OR a.phone LIKE %:keyword%)")
    List<Admin> searchByKeyword(@Param("keyword") String keyword);

    // 统计某角色的管理员数量 - 原有保留
    @Query("SELECT COUNT(a) FROM Admin a WHERE a.roleId = :roleId AND a.status >= 0")
    int countByRoleId(@Param("roleId") Integer roleId);

    // 统计门店管理员数量 - 原有保留
    @Query("SELECT COUNT(a) FROM Admin a WHERE a.storeId = :storeId AND a.status >= 0")
    int countByStoreId(@Param("storeId") Integer storeId);
}