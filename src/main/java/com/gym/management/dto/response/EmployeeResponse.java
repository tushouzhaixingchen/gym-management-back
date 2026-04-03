package com.gym.management.dto.response;

import com.gym.management.entity.Employee;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EmployeeResponse {

    private Integer id;
    private Integer storeId;
    private String storeName;
    private String employeeNo;
    private String realName;
    private Integer gender;
    private String genderText;
    private String phone;
    private String email;
    private String department;
    private String position;
    private LocalDate entryDate;
    private LocalDate leaveDate;
    private BigDecimal baseSalary;
    private Integer status;
    private String statusText;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EmployeeResponse fromEntity(Employee employee, String storeName) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setStoreId(employee.getStoreId());
        response.setStoreName(storeName);
        response.setEmployeeNo(employee.getEmployeeNo());
        response.setRealName(employee.getRealName());
        response.setGender(employee.getGender());
        response.setGenderText(employee.getGenderText());
        response.setPhone(employee.getPhone());
        response.setEmail(employee.getEmail());
        response.setDepartment(employee.getDepartment());
        response.setPosition(employee.getPosition());
        response.setEntryDate(employee.getEntryDate());
        response.setLeaveDate(employee.getLeaveDate());
        response.setBaseSalary(employee.getBaseSalary());
        response.setStatus(employee.getStatus());
        response.setStatusText(employee.getStatusText());
        response.setRemark(employee.getRemark());
        response.setCreatedAt(employee.getCreatedAt());
        response.setUpdatedAt(employee.getUpdatedAt());
        return response;
    }

    public static EmployeeResponse fromEntity(Employee employee) {
        return fromEntity(employee, null);
    }
}