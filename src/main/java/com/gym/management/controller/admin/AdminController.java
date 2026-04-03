// controller/AdminController.java
package com.gym.management.controller.admin;

import com.gym.management.dto.request.admin.AdminCreateRequest;
import com.gym.management.dto.request.admin.AdminUpdateRequest;
import com.gym.management.dto.request.admin.PasswordResetRequest;
import com.gym.management.dto.response.*;
import com.gym.management.common.Result;
import com.gym.management.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 1. 获取管理员列表
    @GetMapping
    public ResponseEntity<Result<PageResult<AdminDTO>>> getAdminList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String roleCode) {
        PageResult<AdminDTO> result = adminService.getAdminList(page, size, keyword, roleCode);
        log.info("获取管理员列表成功，参数：page={}, size={}, keyword={}, roleCode={}", page, size, keyword, roleCode);
        return ResponseEntity.ok(Result.success(result));
    }

    // 2. 获取管理员详情
    @GetMapping("/{id}")
    public ResponseEntity<Result<AdminDetailVO>> getAdminDetail(@PathVariable Integer id) {
        AdminDetailVO vo = adminService.getAdminDetail(id);
        return ResponseEntity.ok(Result.success(vo));
    }

    // 3. 新增管理员
    @PostMapping
    public ResponseEntity<Result<Void>> createAdmin(@RequestBody @Valid AdminCreateRequest request) {
        adminService.createAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success());
    }

    // 4. 编辑管理员
    @PutMapping("/{id}")
    public ResponseEntity<Result<Void>> updateAdmin(
            @PathVariable Integer id,
            @RequestBody @Valid AdminUpdateRequest request) {
        request.setId(id);
        adminService.updateAdmin(request);
        return ResponseEntity.ok(Result.success());
    }

    // 5. 删除管理员
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteAdmin(@PathVariable Integer id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.ok(Result.success());
    }

    // 6. 重置密码
    @PostMapping("/reset-password")
    public ResponseEntity<Result<Void>> resetPassword(@RequestBody @Valid PasswordResetRequest request) {
        adminService.resetPassword(request);
        return ResponseEntity.ok(Result.success());
    }
}