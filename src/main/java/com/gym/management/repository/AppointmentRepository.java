package com.gym.management.repository;

import com.gym.management.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    // 查询会员的预约列表
    List<Appointment> findByMemberIdOrderByCreatedAtDesc(Integer memberId);

    // 查询门店的预约列表
    List<Appointment> findByStoreId(Integer storeId);

    // 查询教练的预约列表
    List<Appointment> findByCoachId(Integer coachId);

    // 查询待确认的预约
    List<Appointment> findByStatus(Integer status);

    // 查询特定时间段内教练的占用时段
    @Query("SELECT a FROM Appointment a WHERE a.coachId = :coachId AND a.timeSlotStart >= :start AND a.timeSlotEnd <= :end AND a.status IN (0, 1)")
    List<Appointment> findOccupiedSlots(@Param("coachId") Integer coachId,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    // 检查教练在指定时间段是否有冲突预约
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.coachId = :coachId AND ((a.timeSlotStart >= :startTime AND a.timeSlotStart < :endTime) OR (a.timeSlotEnd > :startTime AND a.timeSlotEnd <= :endTime) OR (a.timeSlotStart <= :startTime AND a.timeSlotEnd >= :endTime)) AND a.status IN (0, 1)")
    boolean existsByCoachIdAndTimeSlotBetween(@Param("coachId") Integer coachId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // 查询会员待支付的预约
    List<Appointment> findByMemberIdAndStatusAndPayStatus(Integer memberId, Integer status, Integer payStatus);
}
