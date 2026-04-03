package com.gym.management.repository;

import com.gym.management.entity.Coach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoachRepository extends JpaRepository<Coach, Integer> {

    // ================= 【新增/优化】核心登录方法 =================

    /**
     * 🔥 推荐：根据手机号查找【状态正常】的教练
     * 只有 status = 1 的用户才能被查到，用于登录验证
     */
    @Query("SELECT c FROM Coach c WHERE c.phone = :phone AND c.status = 1")
    Optional<Coach> findActiveByPhone(@Param("phone") String phone);

    /**
     * 🔥 推荐：根据教练工号查找【状态正常】的教练
     * 只有 status = 1 的用户才能被查到，用于登录验证
     */
    @Query("SELECT c FROM Coach c WHERE c.coachNo = :coachNo AND c.status = 1")
    Optional<Coach> findActiveByCoachNo(@Param("coachNo") String coachNo);

    // ================= 原有基础方法 (保留用于非登录场景) =================
    // 注意：原有方法不再直接用于登录逻辑，但可保留供后台管理查询所有数据使用

    Optional<Coach> findByPhone(String phone);

    Optional<Coach> findByCoachNo(String coachNo);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Integer id);

    boolean existsByCoachNo(String coachNo);

    // ================= 业务场景查询 (原有保留) =================

    /**
     * 根据门店ID查找所有指定状态的教练
     */
    List<Coach> findByStoreIdAndStatus(Integer storeId, Integer status);

    /**
     * 查找所有在职的自由教练 (无所属门店)
     */
    @Query("SELECT c FROM Coach c WHERE c.storeId IS NULL AND c.status = 1")
    List<Coach> findActiveFreeCoaches();

    /**
     * 根据专长模糊搜索在职教练
     */
    @Query("SELECT c FROM Coach c WHERE c.status = 1 AND c.specialty LIKE %:keyword%")
    List<Coach> searchBySpecialty(@Param("keyword") String keyword);

    /**
     * 统计某门店的在职教练数量
     */
    int countByStoreIdAndStatus(Integer storeId, Integer status);

    /**
     * 统计所有在职教练总数
     */
    int countByStatus(Integer status);
}