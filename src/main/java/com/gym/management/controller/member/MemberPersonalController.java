package com.gym.management.controller.member;

import com.gym.management.common.ResultCode;
import com.gym.management.common.exception.BusinessException;
import com.gym.management.dto.response.AppointmentResponse;
import com.gym.management.dto.response.OrderResponse;
import com.gym.management.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 会员中心 - 我的接口
 * 处理会员个人信息相关的接口（我的预约、我的订单等）
 */
@RestController
@RequestMapping("/api/member")
public class MemberPersonalController {

    @Autowired
    private AppointmentService appointmentService;

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
     * 获取我的预约列表
     */
    @GetMapping("/appointments/my")
    @PreAuthorize("hasRole('MEMBER')")
    public List<AppointmentResponse> getMyAppointments() {
        Integer memberId = getCurrentMemberId();
        return appointmentService.getMyAppointments(memberId);
    }

    /**
     * 获取我的订单列表
     */
    @GetMapping("/orders/my")
    @PreAuthorize("hasRole('MEMBER')")
    public List<OrderResponse> getMyOrders() {
        Integer memberId = getCurrentMemberId();
        return appointmentService.getMyOrders(memberId);
    }
}
