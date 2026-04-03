package com.gym.management.dto.request.admin;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeCreateRequest {

    @NotNull(message = "所属门店ID不能为空")
    private Integer storeId;

    @NotBlank(message = "员工工号不能为空")
    @Size(max = 50, message = "工号长度不能超过50")
    private String employeeNo;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50")
    private String realName;

    @Min(value = 0, message = "性别参数错误")
    @Max(value = 2, message = "性别参数错误")
    private Integer gender;

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    private String email;

    @Size(max = 50, message = "部门长度不能超过50")
    private String department;

    @Size(max = 50, message = "职位长度不能超过50")
    private String position;

    @NotNull(message = "入职日期不能为空")
    private LocalDate entryDate;

    @DecimalMin(value = "0.00", message = "基本工资不能为负数")
    private BigDecimal baseSalary;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}