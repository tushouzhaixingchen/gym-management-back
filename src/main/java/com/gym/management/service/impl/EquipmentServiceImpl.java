package com.gym.management.service.impl;

import com.gym.management.dto.request.admin.*;
import com.gym.management.dto.response.EquipmentResponse;
import com.gym.management.dto.response.EquipmentMaintenanceResponse;
import com.gym.management.entity.Equipment;
import com.gym.management.entity.EquipmentMaintenance;
import com.gym.management.entity.Store;
import com.gym.management.common.exception.BusinessException;
import com.gym.management.repository.EquipmentRepository;
import com.gym.management.repository.EquipmentMaintenanceRepository;
import com.gym.management.repository.StoreRepository;
import com.gym.management.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentMaintenanceRepository equipmentMaintenanceRepository;
    private final StoreRepository storeRepository;

    // ================= 器械管理 =================

    @Override
    public Page<EquipmentResponse> queryEquipments(EquipmentQueryRequest request) {
        int page = Math.max(0, request.getPage() - 1);
        int size = Math.min(request.getSize(), 100);
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<Equipment> spec = (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (request.getStoreId() != null) {
                predicates.add(cb.equal(root.get("storeId"), request.getStoreId()));
            }
            if (request.getEquipmentNo() != null && !request.getEquipmentNo().isEmpty()) {
                predicates.add(cb.like(root.get("equipmentNo"), "%" + request.getEquipmentNo() + "%"));
            }
            if (request.getEquipmentName() != null && !request.getEquipmentName().isEmpty()) {
                predicates.add(cb.like(root.get("equipmentName"), "%" + request.getEquipmentName() + "%"));
            }
            if (request.getEquipmentType() != null && !request.getEquipmentType().isEmpty()) {
                predicates.add(cb.equal(root.get("equipmentType"), request.getEquipmentType()));
            }
            if (request.getBrand() != null && !request.getBrand().isEmpty()) {
                predicates.add(cb.like(root.get("brand"), "%" + request.getBrand() + "%"));
            }
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<Equipment> pageData = equipmentRepository.findAll(spec, pageRequest);
        return pageData.map(equipment -> {
            String storeName = fetchStoreName(equipment.getStoreId());
            return EquipmentResponse.fromEntity(equipment, storeName);
        });
    }

    @Override
    public EquipmentResponse getEquipmentDetail(Integer id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("器械不存在，ID: " + id));
        String storeName = fetchStoreName(equipment.getStoreId());
        return EquipmentResponse.fromEntity(equipment, storeName);
    }

    @Override
    @Transactional
    public EquipmentResponse createEquipment(EquipmentCreateRequest request) {
        // 生成器械编号
        String equipmentNo = generateEquipmentNo();
        while (equipmentRepository.existsByEquipmentNo(equipmentNo)) {
            equipmentNo = generateEquipmentNo();
        }

        Equipment equipment = Equipment.builder()
                .equipmentNo(equipmentNo)
                .storeId(request.getStoreId())
                .equipmentName(request.getEquipmentName())
                .equipmentType(request.getEquipmentType())
                .brand(request.getBrand())
                .model(request.getModel())
                .purchaseDate(request.getPurchaseDate())
                .purchasePrice(request.getPurchasePrice())
                .location(request.getLocation())
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .nextMaintenanceDate(request.getNextMaintenanceDate())
                .remark(request.getRemark())
                .build();

        equipmentRepository.save(equipment);
        String storeName = fetchStoreName(equipment.getStoreId());
        return EquipmentResponse.fromEntity(equipment, storeName);
    }

    @Override
    @Transactional
    public EquipmentResponse updateEquipment(Integer id, EquipmentUpdateRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("器械不存在"));

        if (request.getEquipmentName() != null) equipment.setEquipmentName(request.getEquipmentName());
        if (request.getEquipmentType() != null) equipment.setEquipmentType(request.getEquipmentType());
        if (request.getBrand() != null) equipment.setBrand(request.getBrand());
        if (request.getModel() != null) equipment.setModel(request.getModel());
        if (request.getPurchaseDate() != null) equipment.setPurchaseDate(request.getPurchaseDate());
        if (request.getPurchasePrice() != null) equipment.setPurchasePrice(request.getPurchasePrice());
        if (request.getLocation() != null) equipment.setLocation(request.getLocation());
        if (request.getStatus() != null) equipment.setStatus(request.getStatus());
        if (request.getLastMaintenanceDate() != null) equipment.setLastMaintenanceDate(request.getLastMaintenanceDate());
        if (request.getNextMaintenanceDate() != null) equipment.setNextMaintenanceDate(request.getNextMaintenanceDate());
        if (request.getRemark() != null) equipment.setRemark(request.getRemark());

        equipment.setUpdatedAt(LocalDateTime.now());
        equipmentRepository.save(equipment);

        String storeName = fetchStoreName(equipment.getStoreId());
        return EquipmentResponse.fromEntity(equipment, storeName);
    }

    @Override
    @Transactional
    public void deleteEquipment(Integer id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("器械不存在"));

        // 检查是否有未完成的维护记录
        List<EquipmentMaintenance> pendingMaintenances = equipmentMaintenanceRepository
                .findByEquipmentIdAndStatus(id, 0);
        if (!pendingMaintenances.isEmpty()) {
            throw new BusinessException("该器械有待处理的维护记录，无法删除");
        }

        // 软删除：将状态改为报废
        equipment.setStatus(4);
        equipment.setRemark((equipment.getRemark() == null ? "" : equipment.getRemark())
                + " [已报废:" + LocalDateTime.now() + "]");
        equipment.setUpdatedAt(LocalDateTime.now());
        equipmentRepository.save(equipment);
    }

    // ================= 维护记录管理 =================

    @Override
    public Page<EquipmentMaintenanceResponse> queryMaintenances(EquipmentMaintenanceQueryRequest request) {
        int page = Math.max(0, request.getPage() - 1);
        int size = Math.min(request.getSize(), 100);
        Sort sort = Sort.by(Sort.Direction.DESC, "maintenanceDate");
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<EquipmentMaintenance> spec = (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (request.getStoreId() != null) {
                predicates.add(cb.equal(root.get("storeId"), request.getStoreId()));
            }
            if (request.getEquipmentId() != null) {
                predicates.add(cb.equal(root.get("equipmentId"), request.getEquipmentId()));
            }
            if (request.getMaintenanceType() != null && !request.getMaintenanceType().isEmpty()) {
                predicates.add(cb.equal(root.get("maintenanceType"), request.getMaintenanceType()));
            }
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }
            if (request.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("maintenanceDate"), request.getStartDate()));
            }
            if (request.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("maintenanceDate"), request.getEndDate()));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<EquipmentMaintenance> pageData = equipmentMaintenanceRepository.findAll(spec, pageRequest);
        return pageData.map(maintenance -> {
            String storeName = fetchStoreName(maintenance.getStoreId());
            String equipmentName = fetchEquipmentName(maintenance.getEquipmentId());
            return EquipmentMaintenanceResponse.fromEntity(maintenance, storeName, equipmentName);
        });
    }

    @Override
    public EquipmentMaintenanceResponse getMaintenanceDetail(Integer id) {
        EquipmentMaintenance maintenance = equipmentMaintenanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("维护记录不存在，ID: " + id));
        String storeName = fetchStoreName(maintenance.getStoreId());
        String equipmentName = fetchEquipmentName(maintenance.getEquipmentId());
        return EquipmentMaintenanceResponse.fromEntity(maintenance, storeName, equipmentName);
    }

    @Override
    @Transactional
    public EquipmentMaintenanceResponse createMaintenance(EquipmentMaintenanceCreateRequest request) {
        // 生成维护单号
        String maintenanceNo = generateMaintenanceNo();
        while (equipmentMaintenanceRepository.existsByMaintenanceNo(maintenanceNo)) {
            maintenanceNo = generateMaintenanceNo();
        }

        // 检查器械是否存在
        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> new BusinessException("器械不存在，ID: " + request.getEquipmentId()));

        EquipmentMaintenance maintenance = EquipmentMaintenance.builder()
                .maintenanceNo(maintenanceNo)
                .storeId(request.getStoreId())
                .equipmentId(request.getEquipmentId())
                .maintenanceType(request.getMaintenanceType())
                .maintenanceDate(request.getMaintenanceDate())
                .maintenanceStaff(request.getMaintenanceStaff())
                .maintenanceCost(request.getMaintenanceCost() != null ? request.getMaintenanceCost() : java.math.BigDecimal.ZERO)
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .build();

        equipmentMaintenanceRepository.save(maintenance);

        // 如果开始维修，更新器械状态
        if (request.getStatus() == 1) {
            equipment.setStatus(3); // 维修中
            equipment.setUpdatedAt(LocalDateTime.now());
            equipmentRepository.save(equipment);
        }

        String storeName = fetchStoreName(maintenance.getStoreId());
        String equipmentName = equipment.getEquipmentName();
        return EquipmentMaintenanceResponse.fromEntity(maintenance, storeName, equipmentName);
    }

    @Override
    @Transactional
    public EquipmentMaintenanceResponse updateMaintenance(Integer id, EquipmentMaintenanceUpdateRequest request) {
        EquipmentMaintenance maintenance = equipmentMaintenanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("维护记录不存在"));

        if (request.getMaintenanceType() != null) maintenance.setMaintenanceType(request.getMaintenanceType());
        if (request.getMaintenanceDate() != null) maintenance.setMaintenanceDate(request.getMaintenanceDate());
        if (request.getMaintenanceStaff() != null) maintenance.setMaintenanceStaff(request.getMaintenanceStaff());
        if (request.getMaintenanceCost() != null) maintenance.setMaintenanceCost(request.getMaintenanceCost());
        if (request.getDescription() != null) maintenance.setDescription(request.getDescription());
        if (request.getStatus() != null) maintenance.setStatus(request.getStatus());

        maintenance.setUpdatedAt(LocalDateTime.now());
        equipmentMaintenanceRepository.save(maintenance);

        String storeName = fetchStoreName(maintenance.getStoreId());
        String equipmentName = fetchEquipmentName(maintenance.getEquipmentId());
        return EquipmentMaintenanceResponse.fromEntity(maintenance, storeName, equipmentName);
    }

    @Override
    @Transactional
    public void deleteMaintenance(Integer id) {
        EquipmentMaintenance maintenance = equipmentMaintenanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("维护记录不存在"));

        // 只能删除未完成的记录
        if (maintenance.getStatus() == 2) {
            throw new BusinessException("已完成的维护记录不能删除");
        }

        equipmentMaintenanceRepository.delete(maintenance);
    }

    // ================= 业务操作 =================

    @Override
    @Transactional
    public EquipmentResponse startMaintenance(Integer equipmentId) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new BusinessException("器械不存在"));

        equipment.setStatus(3); // 维修中
        equipment.setUpdatedAt(LocalDateTime.now());
        equipmentRepository.save(equipment);

        String storeName = fetchStoreName(equipment.getStoreId());
        return EquipmentResponse.fromEntity(equipment, storeName);
    }

    @Override
    @Transactional
    public EquipmentResponse completeMaintenance(Integer equipmentId, Integer maintenanceId) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new BusinessException("器械不存在"));

        EquipmentMaintenance maintenance = equipmentMaintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new BusinessException("维护记录不存在"));

        if (!maintenance.getEquipmentId().equals(equipmentId)) {
            throw new BusinessException("维护记录与器械不匹配");
        }

        // 更新维护记录状态
        maintenance.setStatus(2); // 已完成
        maintenance.setUpdatedAt(LocalDateTime.now());
        equipmentMaintenanceRepository.save(maintenance);

        // 更新器械状态和维护日期
        equipment.setStatus(1); // 恢复正常
        equipment.setLastMaintenanceDate(maintenance.getMaintenanceDate());
        equipment.setUpdatedAt(LocalDateTime.now());
        equipmentRepository.save(equipment);

        String storeName = fetchStoreName(equipment.getStoreId());
        return EquipmentResponse.fromEntity(equipment, storeName);
    }

    // ================= 私有辅助方法 =================

    private String fetchStoreName(Integer storeId) {
        if (storeId == null) return "未知门店";
        return storeRepository.findById(storeId)
                .map(Store::getStoreName)
                .orElse("未知门店");
    }

    private String fetchEquipmentName(Integer equipmentId) {
        if (equipmentId == null) return "未知器械";
        return equipmentRepository.findById(equipmentId)
                .map(Equipment::getEquipmentName)
                .orElse("未知器械");
    }

    private String generateEquipmentNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        int random = (int) (Math.random() * 9000) + 1000;
        return "EQ" + dateStr + random;
    }

    private String generateMaintenanceNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        int random = (int) (Math.random() * 9000) + 1000;
        return "MT" + dateStr + random;
    }
}