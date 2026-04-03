// repository/StoreRepository.java
package com.gym.management.repository;

import com.gym.management.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Integer> {

    List<Store> findByStatus(Integer status);
}