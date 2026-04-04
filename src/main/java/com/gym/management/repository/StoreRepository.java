// repository/StoreRepository.java
package com.gym.management.repository;

import com.gym.management.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Integer>, JpaSpecificationExecutor<Store> {

    List<Store> findByStatus(Integer status);
}