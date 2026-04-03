// src/main/java/com/gym/management/repository/MemberVisitRepository.java
package com.gym.management.repository;

import com.gym.management.entity.MemberVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberVisitRepository extends JpaRepository<MemberVisit, Integer> {

    /**
     * 查询会员今日签到记录
     * 优化：直接使用 visitDate (DATE 类型) 匹配今天，无需计算时间范围
     */
    @Query("SELECT v FROM MemberVisit v WHERE v.memberId = :memberId AND v.visitDate = :today")
    Optional<MemberVisit> findTodayCheckIn(
            @Param("memberId") Integer memberId,
            @Param("today") LocalDate today
    );

    /**
     * 查询会员签到记录列表 (按时间倒序)
     * 移除：deleted = 0 条件
     */
    @Query("SELECT v FROM MemberVisit v WHERE v.memberId = :memberId ORDER BY v.checkInTime DESC")
    List<MemberVisit> findMemberCheckInList(@Param("memberId") Integer memberId);

    /**
     * 统计会员总签到次数
     * 移除：deleted = 0 条件
     */
    @Query("SELECT COUNT(v) FROM MemberVisit v WHERE v.memberId = :memberId")
    Long countTotalCheckIns(@Param("memberId") Integer memberId);

    /**
     * 统计会员本月签到次数
     * 优化：使用 visitDate 进行范围查询更直观
     * 移除：deleted = 0 条件
     */
    @Query("SELECT COUNT(v) FROM MemberVisit v " +
            "WHERE v.memberId = :memberId " +
            "AND v.visitDate >= :startDate AND v.visitDate <= :endDate")
    Long countMonthCheckIns(
            @Param("memberId") Integer memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * (可选) 如果需要根据门店查询今日签到列表
     */
    @Query("SELECT v FROM MemberVisit v WHERE v.storeId = :storeId AND v.visitDate = :today ORDER BY v.checkInTime DESC")
    List<MemberVisit> findByStoreIdAndVisitDate(
            @Param("storeId") Integer storeId,
            @Param("today") LocalDate today
    );
}