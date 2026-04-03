// entity/Store.java
package com.gym.management.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "stores")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;

    @Column(name = "store_code", nullable = false, length = 50)
    private String storeCode;

    private String province;
    private String city;
    private String district;
    private String address;
    private String phone;

    @Column(name = "manager_name")
    private String managerName;

    @Column(name = "manager_phone")
    private String managerPhone;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    private Integer status; // 1 营业 0 停业 2 装修中

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}