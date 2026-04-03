package com.gym.management.service;

import com.gym.management.dto.request.admin.EmployeeCreateRequest;
import com.gym.management.dto.request.admin.EmployeeQueryRequest;
import com.gym.management.dto.request.admin.EmployeeUpdateRequest;
import com.gym.management.dto.response.EmployeeResponse;
import org.springframework.data.domain.Page;

public interface EmployeeService {

    /**
     * 分页查询员工列表
     */
    Page<EmployeeResponse> queryEmployees(EmployeeQueryRequest request);

    /**
     * 获取员工详情
     */
    EmployeeResponse getEmployeeDetail(Integer id);

    /**
     * 新增员工
     */
    EmployeeResponse createEmployee(EmployeeCreateRequest request);

    /**
     * 编辑员工
     */
    EmployeeResponse updateEmployee(Integer id, EmployeeUpdateRequest request);

    /**
     * 删除员工
     */
    void deleteEmployee(Integer id);
}