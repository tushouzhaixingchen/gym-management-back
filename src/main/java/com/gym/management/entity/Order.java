package com.gym.management.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "store_id", nullable = false)
    private Integer storeId;

    @Column(name = "order_no", unique = true, nullable = false, length = 50)
    private String orderNo;

    @Column(name = "member_id", nullable = false)
    private Integer memberId;

    @Column(name = "order_type", nullable = false, length = 20)
    private String orderType; // membership/course/appointment/product

    @Column(name = "order_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal orderAmount;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @ColumnDefault("0")
    private BigDecimal discountAmount;

    @Column(name = "pay_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal payAmount;

    @Column(name = "pay_method", length = 20)
    private String payMethod; // wechat/alipay/cash/card

    @Column(name = "pay_status", columnDefinition = "TINYINT DEFAULT 0")
    private Integer payStatus; // 0未支付 1已支付 2已退款

    @Column(name = "pay_time")
    private LocalDateTime payTime;

    @Column(name = "sales_employee_id")
    private Integer salesEmployeeId;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and Setters
}