package com.gym.management.repository;

import com.gym.management.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    // 查询会员的订单（按创建时间倒序）
    List<Order> findByMemberIdOrderByCreatedAtDesc(Integer memberId);

    // 查询预约相关的订单
    List<Order> findByOrderType(String orderType);

    // 查询待支付的订单
    List<Order> findByPayStatus(Integer payStatus);
}