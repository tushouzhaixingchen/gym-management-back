package com.gym.management.controller.admin;

import com.gym.management.dto.request.admin.*;
import com.gym.management.dto.response.EquipmentResponse;
import com.gym.management.dto.response.EquipmentMaintenanceResponse;
import com.gym.management.dto.response.PageResult;
import com.gym.management.common.Result;
import com.gym.management.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // 🔐 权限注解
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 器械管理控制器
 * ⚠️ 所有接口仅限 ADMIN 角色访问
 */
@RestController
@RequestMapping("/api/equipments")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    // ================= 器械管理接口 =================

    /**
     * 获取器械列表
     * GET /api/equipments?page=1&size=10&storeId=1&status=1
     * 🔐 权限：仅 ADMIN
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<PageResult<EquipmentResponse>>> queryEquipments(
            @ModelAttribute @Validated EquipmentQueryRequest request) {

        Page<EquipmentResponse> page = equipmentService.queryEquipments(request);
        PageResult<EquipmentResponse> pageResult = PageResult.of(
                page.getContent(),
                page.getTotalElements(),
                (long) page.getNumber() + 1,
                (long) page.getSize()
        );
        return ResponseEntity.ok(Result.success(pageResult));
    }

    /**
     * 获取器械详情
     * GET /api/equipments/{id}
     * 🔐 权限：仅 ADMIN
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<EquipmentResponse>> getEquipmentDetail(@PathVariable Integer id) {
        EquipmentResponse response = equipmentService.getEquipmentDetail(id);
        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * 新增器械
     * POST /api/equipments
     * 🔐 权限：仅 ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<EquipmentResponse>> createEquipment(
            @RequestBody @Validated EquipmentCreateRequest request) {
        EquipmentResponse response = equipmentService.createEquipment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(response, "器械添加成功"));
    }

    /**
     * 编辑器械
     * PUT /api/equipments/{id}
     * 🔐 权限：仅 ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<EquipmentResponse>> updateEquipment(
            @PathVariable Integer id,
            @RequestBody @Validated EquipmentUpdateRequest request) {
        EquipmentResponse response = equipmentService.updateEquipment(id, request);
        return ResponseEntity.ok(Result.success(response, "器械信息更新成功"));
    }

    /**
     * 删除/报废器械
     * DELETE /api/equipments/{id}
     * 🔐 权限：仅 ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> deleteEquipment(@PathVariable Integer id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.ok(Result.success(null, "器械已报废"));
    }

    /**
     * 开始维修
     * POST /api/equipments/{id}/start-maintenance
     * 🔐 权限：仅 ADMIN
     */
    @PostMapping("/{id}/start-maintenance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<EquipmentResponse>> startMaintenance(@PathVariable Integer id) {
        EquipmentResponse response = equipmentService.startMaintenance(id);
        return ResponseEntity.ok(Result.success(response, "器械已进入维修状态"));
    }

    /**
     * 完成维修
     * POST /api/equipments/{id}/complete-maintenance?maintenanceId=1
     * 🔐 权限：仅 ADMIN
     */
    @PostMapping("/{id}/complete-maintenance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<EquipmentResponse>> completeMaintenance(
            @PathVariable Integer id,
            @RequestParam Integer maintenanceId) {
        EquipmentResponse response = equipmentService.completeMaintenance(id, maintenanceId);
        return ResponseEntity.ok(Result.success(response, "维修已完成"));
    }

    // ================= 维护记录管理接口 =================

    /**
     * 获取维护记录列表
     * GET /api/equipments/maintenances?page=1&size=10&equipmentId=1&status=1
     * 🔐 权限：仅 ADMIN
     */
    @GetMapping("/maintenances")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<PageResult<EquipmentMaintenanceResponse>>> queryMaintenances(
            @ModelAttribute @Validated EquipmentMaintenanceQueryRequest request) {

        Page<EquipmentMaintenanceResponse> page = equipmentService.queryMaintenances(request);
        PageResult<EquipmentMaintenanceResponse> pageResult = PageResult.of(
                page.getContent(),
                page.getTotalElements(),
                (long) page.getNumber() + 1,
                (long) page.getSize()
        );
        return ResponseEntity.ok(Result.success(pageResult));
    }

    /**
     * 获取维护记录详情
     * GET /api/equipments/maintenances/{id}
     * 🔐 权限：仅 ADMIN
     */
    @GetMapping("/maintenances/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<EquipmentMaintenanceResponse>> getMaintenanceDetail(@PathVariable Integer id) {
        EquipmentMaintenanceResponse response = equipmentService.getMaintenanceDetail(id);
        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * 新增维护记录
     * POST /api/equipments/maintenances
     * 🔐 权限：仅 ADMIN
     */
    @PostMapping("/maintenances")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<EquipmentMaintenanceResponse>> createMaintenance(
            @RequestBody @Validated EquipmentMaintenanceCreateRequest request) {
        EquipmentMaintenanceResponse response = equipmentService.createMaintenance(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(response, "维护记录创建成功"));
    }

    /**
     * 编辑维护记录
     * PUT /api/equipments/maintenances/{id}
     * 🔐 权限：仅 ADMIN
     */
    @PutMapping("/maintenances/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<EquipmentMaintenanceResponse>> updateMaintenance(
            @PathVariable Integer id,
            @RequestBody @Validated EquipmentMaintenanceUpdateRequest request) {
        EquipmentMaintenanceResponse response = equipmentService.updateMaintenance(id, request);
        return ResponseEntity.ok(Result.success(response, "维护记录更新成功"));
    }

    /**
     * 删除维护记录
     * DELETE /api/equipments/maintenances/{id}
     * 🔐 权限：仅 ADMIN
     */
    @DeleteMapping("/maintenances/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> deleteMaintenance(@PathVariable Integer id) {
        equipmentService.deleteMaintenance(id);
        return ResponseEntity.ok(Result.success(null, "维护记录已删除"));
    }
}