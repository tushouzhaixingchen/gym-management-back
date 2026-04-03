package com.gym.management.repository;

import com.gym.management.entity.CoachSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CoachScheduleRepository extends JpaRepository<CoachSchedule, Integer> {
    // 查询教练某天的排班
    @Query("SELECT c FROM CoachSchedule c WHERE c.coachId = :coachId AND c.workDate = :date")
    Optional<CoachSchedule> findByCoachAndDate(@Param("coachId") Integer coachId,
                                               @Param("date") LocalDate date);

    // 查询教练某段时间内的排班
    @Query("SELECT c FROM CoachSchedule c WHERE c.coachId = :coachId AND c.workDate >= :startDate AND c.workDate <= :endDate")
    List<CoachSchedule> findByCoachAndDateRange(@Param("coachId") Integer coachId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);
}