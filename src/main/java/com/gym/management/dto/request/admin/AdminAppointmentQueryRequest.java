package com.gym.management.dto.request.admin;

import lombok.Data;

import java.time.LocalDateTime;

// 管理员查询预约列表请求
@Data
public class AdminAppointmentQueryRequest {
    private Integer storeId;
    private Integer status;       // 筛选状态
    private String memberName;    // 会员姓名（前端传参）
    private String keyword;       // 搜索关键词（兼容旧参数）
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    /**
     * 设置 memberName 时同时设置到 keyword 字段
     * 用于支持前端传来的 memberName 参数
     */
    public void setMemberName(String memberName) {
        this.memberName = memberName;
        // 如果 keyword 为空，则使用 memberName 的值
        if (this.keyword == null || this.keyword.isEmpty()) {
            this.keyword = memberName;
        }
    }
}