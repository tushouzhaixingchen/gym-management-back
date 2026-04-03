package com.gym.management.repository;

import com.gym.management.entity.Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Integer>, JpaSpecificationExecutor<Equipment> {

    boolean existsByEquipmentNo(String equipmentNo);

    Optional<Equipment> findByEquipmentNo(String equipmentNo);

    Page<Equipment> findByStoreId(Integer storeId, Pageable pageable);
}