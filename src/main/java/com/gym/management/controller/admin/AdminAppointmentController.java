package com.gym.management.controller.admin;

import com.gym.management.dto.request.admin.AdminAppointmentQueryRequest;
import com.gym.management.dto.request.admin.AppointmentConfirmRequest;
import com.gym.management.dto.request.admin.NoShowRequest;
import com.gym.management.dto.response.*;
import com.gym.management.service.AppointmentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/appointments")
public class AdminAppointmentController {

    private static final Logger log = LoggerFactory.getLogger(AdminAppointmentController.class);

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 分页查询预约列表 (使用 PageResult 包装)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminAppointmentResponse> listAppointments(
            @Valid AdminAppointmentQueryRequest request) {
        log.info("========== 管理员端预约列表接口 ==========");
        log.info("接收到的请求参数：storeId={}, status={}, keyword={}, startDate={}, endDate={}", 
            request.getStoreId(), request.getStatus(), request.getKeyword(), 
            request.getStartDate(), request.getEndDate());
        return appointmentService.getAdminAppointments(request);
    }

    /**
     * 获取预约详情 (使用 AdminDetailVO)
     */
    @GetMapping("/{id}/detail")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminAppointmentResponse getAppointmentDetail(@PathVariable Integer id) {
        return appointmentService.getAppointmentDetail(id);
    }

    /**
     * 确认预约
     */
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public void confirmAppointment(
            @PathVariable Integer id,
            @Valid @RequestBody AppointmentConfirmRequest request) {
        appointmentService.confirmAppointment(request, id);
    }

    /**
     * 标记爽约
     */
    @PutMapping("/{id}/no-show")
    @PreAuthorize("hasRole('ADMIN')")
    public void markNoShow(
            @PathVariable Integer id,
            @Valid @RequestBody NoShowRequest request) {
        appointmentService.markNoShow(id, request);
    }
}