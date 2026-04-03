package com.gym.management.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentResponse {

    private Integer id;
    private Integer storeId;
    private String storeName;
    private String equipmentNo;
    private String equipmentName;
    private String equipmentType;
    private String brand;
    private String model;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private String location;
    private Integer status;
    private String statusText;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EquipmentResponse fromEntity(com.gym.management.entity.Equipment equipment, String storeName) {
        return EquipmentResponse.builder()
                .id(equipment.getId())
                .storeId(equipment.getStoreId())
                .storeName(storeName)
                .equipmentNo(equipment.getEquipmentNo())
                .equipmentName(equipment.getEquipmentName())
                .equipmentType(equipment.getEquipmentType())
                .brand(equipment.getBrand())
                .model(equipment.getModel())
                .purchaseDate(equipment.getPurchaseDate())
                .purchasePrice(equipment.getPurchasePrice())
                .location(equipment.getLocation())
                .status(equipment.getStatus())
                .statusText(getStatusText(equipment.getStatus()))
                .lastMaintenanceDate(equipment.getLastMaintenanceDate())
                .nextMaintenanceDate(equipment.getNextMaintenanceDate())
                .remark(equipment.getRemark())
                .createdAt(equipment.getCreatedAt())
                .updatedAt(equipment.getUpdatedAt())
                .build();
    }

    private static String getStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "正常";
            case 2 -> "使用中";
            case 3 -> "维修中";
            case 4 -> "报废";
            default -> "未知";
        };
    }
}