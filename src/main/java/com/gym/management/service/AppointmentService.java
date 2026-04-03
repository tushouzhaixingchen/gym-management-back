package com.gym.management.service;
import com.gym.management.dto.request.admin.AdminAppointmentQueryRequest;
import com.gym.management.dto.response.AdminAppointmentResponse;
import com.gym.management.dto.request.admin.AppointmentConfirmRequest;
import com.gym.management.dto.request.admin.NoShowRequest;
import com.gym.management.dto.request.member.AppointmentCreateRequest;
import com.gym.management.dto.request.member.CoachListRequest;
import com.gym.management.dto.request.member.PaymentRequest;
import com.gym.management.dto.response.*;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    // --- 会员端功能 ---

    /**
     * 获取教练列表（支持搜索和筛选）
     */
    List<CoachResponse> getCoachList(CoachListRequest request);

    /**
     * 获取教练详情
     */
    CoachResponse getCoachDetail(Integer coachId);

    /**
     * 获取教练空闲时段
     */
    List<TimeSlotResponse> getCoachAvailableSlots(Integer coachId, LocalDate date);

    /**
     * 会员提交预约
     */
    AppointmentResponse createAppointment(AppointmentCreateRequest request, Integer memberId);

    /**
     * 获取我的预约列表
     */
    List<AppointmentResponse> getMyAppointments(Integer memberId);

    /**
     * 支付预约（模拟支付）
     */
    AppointmentResponse payAppointment(Integer appointmentId, PaymentRequest request);

    /**
     * 获取我的订单列表
     */
    List<OrderResponse> getMyOrders(Integer memberId);

    // --- 管理员端功能 ---

    /**
     * 获取预约列表（分页/筛选）
     */
    List<AdminAppointmentResponse> getAdminAppointments(AdminAppointmentQueryRequest request);

    /**
     * 获取预约详情
     */
    AdminAppointmentResponse getAppointmentDetail(Integer appointmentId);

    /**
     * 确认预约
     */
    void confirmAppointment(AppointmentConfirmRequest request, Integer adminId);

    /**
     * 完成预约
     */
    void completeAppointment(Integer appointmentId);

    /**
     * 标记爽约
     */
    void markNoShow(Integer appointmentId, NoShowRequest request);
}