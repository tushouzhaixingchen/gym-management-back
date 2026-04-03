package com.gym.management.dto.request.admin;

import lombok.Data;

@Data
public class EmployeeQueryRequest {

    private Integer page = 1;
    private Integer size = 10;

    private Integer storeId;
    private String employeeNo;
    private String realName;
    private String department;
    private String position;
    private Integer status;
}