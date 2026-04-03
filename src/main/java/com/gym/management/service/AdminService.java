// service/AdminService.java
package com.gym.management.service;

import com.gym.management.dto.request.admin.AdminCreateRequest;
import com.gym.management.dto.request.admin.AdminUpdateRequest;
import com.gym.management.dto.request.admin.PasswordResetRequest;
import com.gym.management.dto.response.*;
import com.gym.management.dto.response.PageResult;

public interface AdminService {

    // 获取管理员列表
    PageResult<AdminDTO> getAdminList(Integer page, Integer size, String keyword, String roleCode);

    // 获取管理员详情
    AdminDetailVO getAdminDetail(Integer id);

    // 新增管理员
    void createAdmin(AdminCreateRequest request);

    // 编辑管理员
    void updateAdmin(AdminUpdateRequest request);

    // 删除管理员
    void deleteAdmin(Integer id);

    // 重置密码
    void resetPassword(PasswordResetRequest request);
}