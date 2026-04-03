package com.gym.management.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentMaintenanceResponse {

    private Integer id;
    private Integer storeId;
    private String storeName;
    private Integer equipmentId;
    private String equipmentName;
    private String maintenanceNo;
    private String maintenanceType;
    private String maintenanceTypeText;
    private LocalDate maintenanceDate;
    private String maintenanceStaff;
    private BigDecimal maintenanceCost;
    private String description;
    private Integer status;
    private String statusText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EquipmentMaintenanceResponse fromEntity(
            com.gym.management.entity.EquipmentMaintenance maintenance,
            String storeName,
            String equipmentName) {
        return EquipmentMaintenanceResponse.builder()
                .id(maintenance.getId())
                .storeId(maintenance.getStoreId())
                .storeName(storeName)
                .equipmentId(maintenance.getEquipmentId())
                .equipmentName(equipmentName)
                .maintenanceNo(maintenance.getMaintenanceNo())
                .maintenanceType(maintenance.getMaintenanceType())
                .maintenanceTypeText(getMaintenanceTypeText(maintenance.getMaintenanceType()))
                .maintenanceDate(maintenance.getMaintenanceDate())
                .maintenanceStaff(maintenance.getMaintenanceStaff())
                .maintenanceCost(maintenance.getMaintenanceCost())
                .description(maintenance.getDescription())
                .status(maintenance.getStatus())
                .statusText(getStatusText(maintenance.getStatus()))
                .createdAt(maintenance.getCreatedAt())
                .updatedAt(maintenance.getUpdatedAt())
                .build();
    }

    private static String getMaintenanceTypeText(String type) {
        if (type == null) return "未知";
        return switch (type) {
            case "定期" -> "定期维护";
            case "故障" -> "故障维修";
            case "大修" -> "大修";
            default -> type;
        };
    }

    private static String getStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待处理";
            case 1 -> "处理中";
            case 2 -> "已完成";
            default -> "未知";
        };
    }
}