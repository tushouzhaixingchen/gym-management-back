package com.gym.management.dto.request.admin;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EquipmentUpdateRequest {

    @Size(max = 100, message = "器械名称最多100字符")
    private String equipmentName;

    private String equipmentType;
    private String brand;
    private String model;
    private LocalDate purchaseDate;

    @DecimalMin(value = "0.00", message = "购买价格不能为负数")
    private BigDecimal purchasePrice;

    @Size(max = 100, message = "放置位置最多100字符")
    private String location;

    private Integer status;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;

    @Size(max = 500, message = "备注最多500字符")
    private String remark;
}