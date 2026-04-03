package com.gym.management.dto.request.admin;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EquipmentMaintenanceUpdateRequest {

    private String maintenanceType;
    private LocalDate maintenanceDate;
    private String maintenanceStaff;

    @DecimalMin(value = "0.00", message = "维护费用不能为负数")
    private BigDecimal maintenanceCost;

    @Size(max = 1000, message = "维护描述最多1000字符")
    private String description;

    private Integer status;
}