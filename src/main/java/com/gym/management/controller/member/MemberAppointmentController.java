package com.gym.management.controller.member;

import com.gym.management.common.ResultCode;
import com.gym.management.common.exception.BusinessException;
import com.gym.management.dto.request.member.AppointmentCreateRequest;
import com.gym.management.dto.request.member.CoachListRequest;
import com.gym.management.dto.request.member.PaymentRequest;
import com.gym.management.dto.response.*;
import com.gym.management.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.*;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/member/appointments")
public class MemberAppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 获取教练列表 (分页或列表)
     */
    @GetMapping("/coaches")
    @PreAuthorize("hasRole('MEMBER')")
    public List<CoachResponse> listCoaches(
            @Valid CoachListRequest request) {
        return appointmentService.getCoachList(request);
    }

    /**
     * 获取教练详情
     */
    @GetMapping("/coaches/{coachId}")
    @PreAuthorize("hasRole('MEMBER')")
    public CoachResponse getCoachDetail(
            @PathVariable Integer coachId) {
        return appointmentService.getCoachDetail(coachId);
    }

    /**
     * 获取教练空闲时段
     */
    @GetMapping("/coaches/{coachId}/slots")
    @PreAuthorize("hasRole('MEMBER')")
    public List<TimeSlotResponse> getCoachSlots(
            @PathVariable Integer coachId,
            @RequestParam LocalDate date) {
        return appointmentService.getCoachAvailableSlots(coachId, date);
    }

    /**
     * 创建预约
     */
    @PostMapping
    @PreAuthorize("hasRole('MEMBER')")
    public AppointmentResponse createAppointment(
            @Valid @RequestBody AppointmentCreateRequest request,
            Principal principal) {
        // 从 Spring Security 上下文中获取真实的登录用户 ID
        Integer memberId = getCurrentMemberId();
        return appointmentService.createAppointment(request, memberId);
    }

    /**
     * 获取当前登录会员的 ID
     * 从 JWT Token 解析并存入 SecurityContext 的用户 ID
     */
    private Integer getCurrentMemberId() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        
        if (principal instanceof Integer) {
            return (Integer) principal;
        } else {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }

    /**
     * 模拟支付
     */
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('MEMBER')")
    public AppointmentResponse payAppointment(
            @PathVariable Integer id,
            @Valid @RequestBody PaymentRequest request) {
        return appointmentService.payAppointment(id, request);
    }

}