package com.gym.management.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 订单响应
@Data
public class OrderResponse {
    private Integer id;
    private String orderNo;
    private Integer memberId;
    private String orderType; // "appointment"/"membership"/"course"
    private BigDecimal orderAmount;      // 订单金额
    private BigDecimal discountAmount;   // 优惠金额
    private BigDecimal payAmount;        // 实付金额
    private Integer payStatus;           // 0 未支付 1 已支付 2 已退款
    private String payStatusDesc;        // 支付状态描述
    private String payMethod;            // wechat/alipay/cash/card
    private LocalDateTime payTime;
    private LocalDateTime createdAt;
    private String remark;
}