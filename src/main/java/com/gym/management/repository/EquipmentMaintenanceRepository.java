package com.gym.management.repository;

import com.gym.management.entity.EquipmentMaintenance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentMaintenanceRepository extends JpaRepository<EquipmentMaintenance, Integer>, JpaSpecificationExecutor<EquipmentMaintenance> {

    boolean existsByMaintenanceNo(String maintenanceNo);

    Optional<EquipmentMaintenance> findByMaintenanceNo(String maintenanceNo);

    Page<EquipmentMaintenance> findByEquipmentId(Integer equipmentId, Pageable pageable);

    Page<EquipmentMaintenance> findByStoreId(Integer storeId, Pageable pageable);

    List<EquipmentMaintenance> findByEquipmentIdAndStatus(Integer equipmentId, Integer status);
}