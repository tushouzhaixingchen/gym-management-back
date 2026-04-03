package com.gym.management.dto.request.member;

import lombok.Data;

// 支付请求
@Data
public class PaymentRequest {
    private Integer payMethod; // 1微信 2支付宝 3现金

    // Getters and Setters
}