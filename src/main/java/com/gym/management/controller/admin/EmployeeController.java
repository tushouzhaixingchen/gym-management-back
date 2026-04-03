package com.gym.management.controller.admin;

import com.gym.management.dto.request.admin.EmployeeCreateRequest;
import com.gym.management.dto.request.admin.EmployeeQueryRequest;
import com.gym.management.dto.request.admin.EmployeeUpdateRequest;
import com.gym.management.dto.response.EmployeeResponse;
import com.gym.management.service.EmployeeService;
import com.gym.management.dto.response.PageResult;
import com.gym.management.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // 🔥 1. 引入权限注解
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 员工管理控制器
 * ⚠️ 严格模式：所有接口仅限 ADMIN 角色访问
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * 获取员工列表
     * GET /api/employees?page=1&size=10&storeId=1&status=1
     * 🔐 权限：仅 ADMIN
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<PageResult<EmployeeResponse>>> queryEmployees(
            @ModelAttribute @Validated EmployeeQueryRequest request) {

        Page<EmployeeResponse> page = employeeService.queryEmployees(request);
        PageResult<EmployeeResponse> pageResult = PageResult.of(
                page.getContent(),
                page.getTotalElements(),
                (long) page.getNumber() + 1,
                (long) page.getSize()
        );
        return ResponseEntity.ok(Result.success(pageResult));
    }

    /**
     * 获取员工详情
     * GET /api/employees/{id}
     * 🔐 权限：仅 ADMIN
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<EmployeeResponse>> getEmployeeDetail(@PathVariable Integer id) {
        EmployeeResponse response = employeeService.getEmployeeDetail(id);
        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * 新增员工
     * POST /api/employees
     * 🔐 权限：仅 ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<EmployeeResponse>> createEmployee(
            @RequestBody @Validated EmployeeCreateRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        // 创建成功返回 201
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(response, "员工添加成功"));
    }

    /**
     * 编辑员工
     * PUT /api/employees/{id}
     * 🔐 权限：仅 ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<EmployeeResponse>> updateEmployee(
            @PathVariable Integer id,
            @RequestBody @Validated EmployeeUpdateRequest request) {
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(Result.success(response, "员工信息更新成功"));
    }

    /**
     * 删除员工
     * DELETE /api/employees/{id}
     * 🔐 权限：仅 ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> deleteEmployee(@PathVariable Integer id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(Result.success());
    }
}