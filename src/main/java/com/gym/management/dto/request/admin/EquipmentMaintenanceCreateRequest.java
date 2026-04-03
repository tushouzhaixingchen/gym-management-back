package com.gym.management.dto.request.admin;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EquipmentMaintenanceCreateRequest {

    @NotNull(message = "门店ID不能为空")
    private Integer storeId;

    @NotNull(message = "器械ID不能为空")
    private Integer equipmentId;

    @NotBlank(message = "维护类型不能为空")
    private String maintenanceType; // 定期/故障/大修

    @NotNull(message = "维护日期不能为空")
    private LocalDate maintenanceDate;

    @Size(max = 50, message = "维护人员最多50字符")
    private String maintenanceStaff;

    @DecimalMin(value = "0.00", message = "维护费用不能为负数")
    private BigDecimal maintenanceCost = BigDecimal.ZERO;

    @Size(max = 1000, message = "维护描述最多1000字符")
    private String description;

    private Integer status = 1;
}