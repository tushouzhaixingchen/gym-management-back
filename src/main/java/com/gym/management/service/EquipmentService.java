package com.gym.management.service;

import com.gym.management.dto.request.admin.*;
import com.gym.management.dto.response.EquipmentResponse;
import com.gym.management.dto.response.EquipmentMaintenanceResponse;
import org.springframework.data.domain.Page;

public interface EquipmentService {

    // ========== 器械管理 ==========
    Page<EquipmentResponse> queryEquipments(EquipmentQueryRequest request);

    EquipmentResponse getEquipmentDetail(Integer id);

    EquipmentResponse createEquipment(EquipmentCreateRequest request);

    EquipmentResponse updateEquipment(Integer id, EquipmentUpdateRequest request);

    void deleteEquipment(Integer id);

    // ========== 维护记录管理 ==========
    Page<EquipmentMaintenanceResponse> queryMaintenances(EquipmentMaintenanceQueryRequest request);

    EquipmentMaintenanceResponse getMaintenanceDetail(Integer id);

    EquipmentMaintenanceResponse createMaintenance(EquipmentMaintenanceCreateRequest request);

    EquipmentMaintenanceResponse updateMaintenance(Integer id, EquipmentMaintenanceUpdateRequest request);

    void deleteMaintenance(Integer id);

    // ========== 业务操作 ==========
    EquipmentResponse startMaintenance(Integer equipmentId);

    EquipmentResponse completeMaintenance(Integer equipmentId, Integer maintenanceId);
}