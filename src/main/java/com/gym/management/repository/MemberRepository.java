package com.gym.management.repository;

import com.gym.management.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer>, JpaSpecificationExecutor<Member> {

    // ================= 【新增/优化】用于登录的核心方法 =================

    /**
     * 🔥 推荐：根据手机号 或 会员卡号 查找【状态正常】的会员
     * 只有 status = 1 的用户才能被查到，用于登录验证
     *
     * @param phone 手机号
     * @param memberNo 会员卡号
     * @return Optional<Member>
     */
    @Query("SELECT m FROM Member m WHERE " +
            "(m.phone = :phone OR m.memberNo = :memberNo) AND " +
            "m.status = 1")
    Optional<Member> findActiveByPhoneOrMemberNo(
            @Param("phone") String phone,
            @Param("memberNo") String memberNo
    );

    /**
     * 原有方法保留（用于非登录场景，如后台管理员查看详细信息时可能需要看禁用用户）
     */
    Optional<Member> findByPhoneOrMemberNo(String phone, String memberNo);

    // ================= 基础校验与查找 (原有保留) =================

    Optional<Member> findByPhone(String phone);

    Optional<Member> findByMemberNo(String memberNo);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Integer id);

    boolean existsByMemberNo(String memberNo);

    // ================= 业务场景自定义查询 (原有保留) =================

    @Query("SELECT m FROM Member m WHERE " +
            "m.status = 1 AND " +
            "m.expireDate IS NOT NULL AND " +
            "m.expireDate >= :startDate AND " +
            "m.expireDate <= :endDate")
    List<Member> findExpiringMembers(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT m FROM Member m WHERE " +
            "m.status = 1 AND " +
            "m.expireDate IS NOT NULL AND " +
            "m.expireDate < :currentDate")
    List<Member> findExpiredMembers(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT m.status, COUNT(m) FROM Member m GROUP BY m.status")
    List<Object[]> countGroupByStatus();

    @Query("SELECT m.id FROM Member m WHERE " +
            "m.lastVisitAt >= :startTime AND " +
            "m.lastVisitAt < :endTime")
    List<Integer> findIdsCheckedInBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT m FROM Member m WHERE m.balance > :minBalance ORDER BY m.balance DESC")
    List<Member> findByBalanceGreaterThan(@Param("minBalance") java.math.BigDecimal minBalance);

    @Query("SELECT m FROM Member m WHERE " +
            "m.cardType = 'times' AND " +
            "m.remainingTimes IS NOT NULL AND " +
            "m.remainingTimes <= :maxTimes")
    List<Member> findLowBalanceTimesCards(@Param("maxTimes") Integer maxTimes);
}