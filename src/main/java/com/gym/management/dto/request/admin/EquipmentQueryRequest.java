package com.gym.management.dto.request.admin;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Data
public class EquipmentQueryRequest {

    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer size = 10;

    private Integer storeId;
    private String equipmentNo;
    private String equipmentName;
    private String equipmentType;
    private String brand;
    private Integer status;
}