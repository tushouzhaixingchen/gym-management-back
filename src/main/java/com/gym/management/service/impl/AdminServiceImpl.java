// service/impl/AdminServiceImpl.java
package com.gym.management.service.impl;

import com.gym.management.dto.request.admin.AdminCreateRequest;
import com.gym.management.dto.request.admin.AdminUpdateRequest;
import com.gym.management.dto.request.admin.PasswordResetRequest;
import com.gym.management.dto.response.*;
import com.gym.management.entity.*;
import com.gym.management.common.exception.BusinessException;
import com.gym.management.common.ResultCode;
import com.gym.management.repository.*;
import com.gym.management.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final RoleRepository roleRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;

    // 获取当前登录管理员（从 SecurityContext 获取）
    private Admin getCurrentAdmin() {
        Object principal = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        if (!(principal instanceof Integer)) {
            log.warn("【权限检查】Principal 类型错误：{}", principal.getClass().getName());
            return null;
        }

        Integer adminId = (Integer)principal;
        Admin admin = adminRepository.findById(adminId).orElse(null);
        
        if (admin != null) {
            log.info("【权限检查】当前登录管理员：id={}, username={}, roleId={}", 
                admin.getId(), admin.getUsername(), admin.getRoleId());
        } else {
            log.warn("【权限检查】未找到管理员信息：id={}", adminId);
        }
        
        return admin;
    }

    // 检查是否为超级管理员
    private boolean isSuperAdmin(Admin admin) {
        if (admin == null) {
            log.warn("【权限检查】管理员为 null，不是超级管理员");
            return false;
        }
        
        // 🔴 方式 1：直接通过 roleId=1 判断（推荐，性能更好）
        if (admin.getRoleId() != null && admin.getRoleId() == 1) {
            log.info("【权限检查】✓ 是超级管理员 (roleId=1): id={}, username={}", 
                admin.getId(), admin.getUsername());
            return true;
        }
        
        // 方式 2：通过查询角色表判断 role_code
        Role role = roleRepository.findById(admin.getRoleId()).orElse(null);
        if(role != null){
            log.info("【权限检查】当前管理员角色：{}, roleId={}, 是否超级管理员：{}", 
                role.getRoleCode(), admin.getRoleId(), "super_admin".equals(role.getRoleCode()));
            return "super_admin".equals(role.getRoleCode());
        }
        
        log.warn("【权限检查】未找到角色信息：roleId={}", admin.getRoleId());
        return false;
    }

    @Override
    public PageResult<AdminDTO> getAdminList(Integer page, Integer size, String keyword, String roleCode) {
        Admin currentAdmin = getCurrentAdmin();

        // 🔴 权限校验
        if (!isSuperAdmin(currentAdmin)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "只有超级管理员可以查看管理员列表");
        }

        // 分页查询
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Admin> adminPage;

        if (keyword != null && !keyword.isEmpty()) {
            // 简单实现：先查询再过滤（生产环境建议用 Specification）
            List<Admin> all = adminRepository.searchByKeyword(keyword);
            adminPage = new org.springframework.data.domain.PageImpl<>(all, pageRequest, all.size());
        } else {
            adminPage = adminRepository.findAll(pageRequest);
        }

        // Entity 转 DTO
        List<AdminDTO> dtoList = adminPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtoList, adminPage.getTotalElements(), page.longValue(), size.longValue());
    }

    @Override
    public AdminDetailVO getAdminDetail(Integer id) {
        Admin currentAdmin = getCurrentAdmin();
        Admin targetAdmin = adminRepository.findByIdAndStatusNormal(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "管理员不存在"));

        // 🔴 权限校验
        if (!isSuperAdmin(currentAdmin)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权查看他人信息");
        }

        return convertToDetailVO(targetAdmin);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createAdmin(AdminCreateRequest request) {
        Admin currentAdmin = getCurrentAdmin();

        // 🔴 权限校验
        if (!isSuperAdmin(currentAdmin)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "只有超级管理员可以创建新管理员");
        }

        // 🔴 校验角色
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "角色不存在"));

        // 🔴 门店管理员必须绑定门店
        if ("store_admin".equals(role.getRoleCode())) {
            if (request.getStoreId() == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "门店管理员必须绑定门店");
            }
            Store store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "门店不存在"));
            if (store.getStatus() != 1) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "门店已停业或装修中");
            }
        } else {
            request.setStoreId(null);
        }

        // 🔴 检查用户名是否已存在
        if (adminRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户名已存在");
        }

        // 构建实体
        Admin newAdmin = new Admin();
        newAdmin.setUsername(request.getUsername());
        newAdmin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newAdmin.setRealName(request.getRealName());
        newAdmin.setRoleId(request.getRoleId());
        newAdmin.setStoreId(request.getStoreId());
        newAdmin.setPhone(request.getPhone());
        newAdmin.setEmail(request.getEmail());
        newAdmin.setStatus(1);
        newAdmin.setCreatedAt(LocalDateTime.now());
        newAdmin.setUpdatedAt(LocalDateTime.now());

        adminRepository.save(newAdmin);
        log.info("创建新管理员：username={}, roleId={}, storeId={}",
                request.getUsername(), request.getRoleId(), request.getStoreId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAdmin(AdminUpdateRequest request) {
        Admin currentAdmin = getCurrentAdmin();
        Admin targetAdmin = adminRepository.findByIdAndStatusNormal(request.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "管理员不存在"));

        // 🔴 权限校验
        if (!isSuperAdmin(currentAdmin)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权修改他人信息");
            // 防止提权
        }

        // 🔴 防止修改超级管理员的角色
        Role targetRole = roleRepository.findById(targetAdmin.getRoleId()).orElse(null);
        if (targetRole != null && "super_admin".equals(targetRole.getRoleCode())) {
            request.setRoleId(targetAdmin.getRoleId());
            request.setStoreId(null);
        }

        // 更新字段
        if (request.getRealName() != null) targetAdmin.setRealName(request.getRealName());
        if (request.getRoleId() != null) targetAdmin.setRoleId(request.getRoleId());
        if (request.getStoreId() != null) targetAdmin.setStoreId(request.getStoreId());
        if (request.getPhone() != null) targetAdmin.setPhone(request.getPhone());
        if (request.getEmail() != null) targetAdmin.setEmail(request.getEmail());
        if (request.getStatus() != null) targetAdmin.setStatus(request.getStatus());

        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            targetAdmin.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        targetAdmin.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(targetAdmin);

        log.info("更新管理员信息：id={}, username={}", targetAdmin.getId(), targetAdmin.getUsername());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAdmin(Integer id) {
        Admin currentAdmin = getCurrentAdmin();

        // 🔴 权限校验
        if (!isSuperAdmin(currentAdmin)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "只有超级管理员可以删除管理员");
        }

        // 🔴 防止删除自己
        if (currentAdmin.getId().equals(id)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "不能删除自己的账号");
        }

        Admin targetAdmin = adminRepository.findByIdAndStatusNormal(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "管理员不存在"));

        // 🔴 防止删除最后一个超级管理员
        int superAdminCount = adminRepository.countByRoleId(1);
        Role targetRole = roleRepository.findById(targetAdmin.getRoleId()).orElse(null);
        if (targetRole != null && "super_admin".equals(targetRole.getRoleCode()) && superAdminCount <= 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "不能删除最后一个超级管理员");
        }

        // 逻辑删除
        targetAdmin.setStatus(-1);
        targetAdmin.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(targetAdmin);

        log.info("删除管理员 ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(PasswordResetRequest request) {
        Admin currentAdmin = getCurrentAdmin();

        // 🔴 权限校验
        if (!isSuperAdmin(currentAdmin)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "只有超级管理员可以重置密码");
        }

        Admin targetAdmin = adminRepository.findByIdAndStatusNormal(request.getAdminId())
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "管理员不存在"));

        targetAdmin.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        targetAdmin.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(targetAdmin);

        log.info("重置管理员密码：username={}", targetAdmin.getUsername());
    }

    // ==================== 转换方法 ====================

    private AdminDTO convertToDTO(Admin admin) {
        AdminDTO dto = new AdminDTO();
        dto.setId(admin.getId());
        dto.setUsername(admin.getUsername());
        dto.setRealName(admin.getRealName());
        dto.setRoleId(admin.getRoleId());
        dto.setStoreId(admin.getStoreId());
        dto.setPhone(admin.getPhone());
        dto.setEmail(admin.getEmail());
        dto.setStatus(admin.getStatus());
        dto.setLastLoginAt(admin.getLastLoginAt());
        dto.setCreatedAt(admin.getCreatedAt());

        // 关联查询角色和门店信息
        Role role = roleRepository.findById(admin.getRoleId()).orElse(null);
        if (role != null) {
            dto.setRoleName(role.getRoleName());
            dto.setRoleCode(role.getRoleCode());
        }

        if (admin.getStoreId() != null) {
            storeRepository.findById(admin.getStoreId()).ifPresent(store -> dto.setStoreName(store.getStoreName()));
        }

        return dto;
    }

    private AdminDetailVO convertToDetailVO(Admin admin) {
        AdminDetailVO vo = new AdminDetailVO();
        vo.setId(admin.getId());
        vo.setUsername(admin.getUsername());
        vo.setRealName(admin.getRealName());
        vo.setRoleId(admin.getRoleId());
        vo.setStoreId(admin.getStoreId());
        vo.setPhone(admin.getPhone());
        vo.setEmail(admin.getEmail());
        vo.setStatus(admin.getStatus());
        vo.setLastLoginAt(admin.getLastLoginAt());
        vo.setLastLoginIp(admin.getLastLoginIp());
        vo.setCreatedAt(admin.getCreatedAt());
        vo.setUpdatedAt(admin.getUpdatedAt());

        Role role = roleRepository.findById(admin.getRoleId()).orElse(null);
        if (role != null) {
            vo.setRoleName(role.getRoleName());
            vo.setRoleCode(role.getRoleCode());
        }

        if (admin.getStoreId() != null) {
            Store store = storeRepository.findById(admin.getStoreId()).orElse(null);
            if (store != null) {
                vo.setStoreName(store.getStoreName());
            }
        }

        return vo;
    }
}