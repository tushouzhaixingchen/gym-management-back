package com.gym.management.repository;

import com.gym.management.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmployeeNo(String employeeNo);

    boolean existsByEmployeeNo(String employeeNo);

    Optional<Employee> findByPhone(String phone);

    boolean existsByPhone(String phone);

    Page<Employee> findByStoreIdAndStatus(Integer storeId, Integer status, Pageable pageable);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.storeId = :storeId AND e.status = 1")
    Long countActiveByStoreId(@Param("storeId") Integer storeId);
}