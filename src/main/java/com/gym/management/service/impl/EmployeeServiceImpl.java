package com.gym.management.service.impl;

import com.gym.management.dto.request.admin.EmployeeCreateRequest;
import com.gym.management.dto.request.admin.EmployeeQueryRequest;
import com.gym.management.dto.request.admin.EmployeeUpdateRequest;
import com.gym.management.dto.response.EmployeeResponse;
import com.gym.management.entity.Employee;
import com.gym.management.entity.Store;
import com.gym.management.entity.Admin;
import com.gym.management.common.exception.BusinessException;
import com.gym.management.repository.EmployeeRepository;
import com.gym.management.repository.StoreRepository;
import com.gym.management.repository.AdminRepository;
import com.gym.management.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final AdminRepository adminRepository;

    // ================= 辅助方法：获取当前登录管理员信息 =================

    /**
     * 获取当前登录管理员的 ID（从 SecurityContext）
     */
    private Integer getCurrentAdminId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (principal instanceof Integer) {
            return (Integer) principal;
        } else {
            log.warn("【权限检查】Principal 不是 Integer 类型：{}", principal.getClass().getName());
            return null;
        }
    }

    /**
     * 获取当前登录管理员的信息
     * @return Admin 对象，如果未找到返回 null
     */
    private Admin getCurrentAdmin() {
        Integer adminId = getCurrentAdminId();
        if (adminId == null) return null;
        return adminRepository.findById(adminId).orElse(null);
    }

    /**
     * 检查是否为超级管理员（roleId=1）
     */
    private boolean isSuperAdmin(Admin admin) {
        if (admin == null) return false;
        return admin.getRoleId() != null && admin.getRoleId() == 1;
    }

    @Override
    public Page<EmployeeResponse> queryEmployees(EmployeeQueryRequest request) {
        // 🔴 权限检查：获取当前登录管理员信息
        Admin currentAdmin = getCurrentAdmin();
        boolean isSuperAdmin = isSuperAdmin(currentAdmin);
        Integer currentStoreId = currentAdmin != null ? currentAdmin.getStoreId() : null;
        
        log.info("【权限检查】员工列表查询 - 管理员 ID: {}, 用户名：{}, 角色 ID: {}, 是否超级管理员：{}", 
            currentAdmin != null ? currentAdmin.getId() : "null",
            currentAdmin != null ? currentAdmin.getUsername() : "null",
            currentAdmin != null ? currentAdmin.getRoleId() : "null",
            isSuperAdmin);
        
        // 确定最终使用的门店 ID
        final Integer effectiveStoreId;
        
        if (isSuperAdmin) {
            effectiveStoreId = request.getStoreId();
            if (effectiveStoreId == null) {
                log.info("【权限检查】✓ 超级管理员模式：查询所有门店的员工");
            } else {
                log.info("【权限检查】✓ 超级管理员模式：查询指定门店 ID={} 的员工", effectiveStoreId);
            }
        } else if (currentStoreId != null) {
            effectiveStoreId = currentStoreId;
            log.info("【权限检查】✓ 门店管理员模式：只能查询本店 ID={} 的员工", effectiveStoreId);
        } else {
            effectiveStoreId = null;
            log.warn("【权限检查】✗ 当前用户既不是超级管理员也没有门店 ID，无权访问");
        }
        
        int page = Math.max(0, request.getPage() - 1);
        int size = Math.min(request.getSize(), 100);
        Sort sort = Sort.by(Sort.Direction.DESC, "entryDate");
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<Employee> spec = (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();

            // 🔴 添加门店权限过滤
            if (effectiveStoreId != null) {
                predicates.add(cb.equal(root.get("storeId"), effectiveStoreId));
                log.debug("【权限过滤】添加门店条件：storeId={}", effectiveStoreId);
            }
            
            if (request.getEmployeeNo() != null && !request.getEmployeeNo().isEmpty()) {
                predicates.add(cb.like(root.get("employeeNo"), "%" + request.getEmployeeNo() + "%"));
            }
            if (request.getRealName() != null && !request.getRealName().isEmpty()) {
                predicates.add(cb.like(root.get("realName"), "%" + request.getRealName() + "%"));
            }
            if (request.getDepartment() != null && !request.getDepartment().isEmpty()) {
                predicates.add(cb.equal(root.get("department"), request.getDepartment()));
            }
            if (request.getPosition() != null && !request.getPosition().isEmpty()) {
                predicates.add(cb.equal(root.get("position"), request.getPosition()));
            }
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<Employee> pageData = employeeRepository.findAll(spec, pageRequest);
        return pageData.map(employee -> {
            String storeName = fetchStoreName(employee.getStoreId());
            return EmployeeResponse.fromEntity(employee, storeName);
        });
    }

    @Override
    public EmployeeResponse getEmployeeDetail(Integer id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("员工不存在，ID: " + id));
        String storeName = fetchStoreName(employee.getStoreId());
        return EmployeeResponse.fromEntity(employee, storeName);
    }

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        if (employeeRepository.existsByEmployeeNo(request.getEmployeeNo())) {
            throw new BusinessException("该工号已被使用：" + request.getEmployeeNo());
        }

        if (employeeRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("该手机号已被其他员工使用：" + request.getPhone());
        }

        storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new BusinessException("门店不存在，ID: " + request.getStoreId()));

        Employee employee = new Employee();
        employee.setStoreId(request.getStoreId());
        employee.setEmployeeNo(request.getEmployeeNo());
        employee.setRealName(request.getRealName());
        employee.setGender(request.getGender() != null ? request.getGender() : 0);
        employee.setPhone(request.getPhone());
        employee.setEmail(request.getEmail());
        employee.setDepartment(request.getDepartment());
        employee.setPosition(request.getPosition());
        employee.setEntryDate(request.getEntryDate());
        employee.setBaseSalary(request.getBaseSalary());
        employee.setStatus(1);
        employee.setRemark(request.getRemark());
        employee.setCreatedAt(LocalDateTime.now());

        Employee saved = employeeRepository.save(employee);
        String storeName = fetchStoreName(saved.getStoreId());
        return EmployeeResponse.fromEntity(saved, storeName);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Integer id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("员工不存在，ID: " + id));

        if (request.getRealName() != null) {
            employee.setRealName(request.getRealName());
        }
        if (request.getPhone() != null) {
            if (!employee.getPhone().equals(request.getPhone())
                    && employeeRepository.existsByPhone(request.getPhone())) {
                throw new BusinessException("该手机号已被其他员工使用");
            }
            employee.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            employee.setEmail(request.getEmail());
        }
        if (request.getDepartment() != null) {
            employee.setDepartment(request.getDepartment());
        }
        if (request.getPosition() != null) {
            employee.setPosition(request.getPosition());
        }
        if (request.getGender() != null) {
            employee.setGender(request.getGender());
        }
        if (request.getLeaveDate() != null) {
            employee.setLeaveDate(request.getLeaveDate());
            if (employee.getStatus() == 1) {
                employee.setStatus(0);
            }
        }
        if (request.getBaseSalary() != null) {
            employee.setBaseSalary(request.getBaseSalary());
        }
        if (request.getRemark() != null) {
            employee.setRemark(request.getRemark());
        }

        employee.setUpdatedAt(LocalDateTime.now());

        Employee saved = employeeRepository.save(employee);
        return EmployeeResponse.fromEntity(saved, fetchStoreName(saved.getStoreId()));
    }

    @Override
    @Transactional
    public void deleteEmployee(Integer id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("员工不存在，ID: " + id));

        employee.setStatus(0);
        employee.setLeaveDate(LocalDate.now());
        employee.setRemark((employee.getRemark() == null ? "" : employee.getRemark())
                + " [已删除:" + LocalDateTime.now() + "]");
        employee.setUpdatedAt(LocalDateTime.now());

        employeeRepository.save(employee);
    }

    private String fetchStoreName(Integer storeId) {
        if (storeId == null) return "未知门店";
        return storeRepository.findById(storeId)
                .map(Store::getStoreName)
                .orElse("未知门店");
    }
}