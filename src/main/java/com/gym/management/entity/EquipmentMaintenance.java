package com.gym.management.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "equipment_maintenances")
public class EquipmentMaintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "store_id", nullable = false)
    private Integer storeId;

    @Column(name = "equipment_id", nullable = false)
    private Integer equipmentId;

    @Column(name = "maintenance_no", unique = true, nullable = false, length = 50)
    private String maintenanceNo;

    @Column(name = "maintenance_type", nullable = false, length = 20)
    private String maintenanceType; // 定期/故障/大修

    @Column(name = "maintenance_date", nullable = false)
    private LocalDate maintenanceDate;

    @Column(name = "maintenance_staff", length = 50)
    private String maintenanceStaff;

    @Column(name = "maintenance_cost", precision = 10, scale = 2)
    private BigDecimal maintenanceCost = BigDecimal.ZERO;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status")
    private Integer status = 1; // 0待处理 1处理中 2已完成

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", insertable = false, updatable = false)
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", insertable = false, updatable = false)
    private Store store;
}