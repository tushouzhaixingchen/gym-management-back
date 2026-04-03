package com.gym.management.dto.request.member;

import lombok.Data;
import java.time.LocalDate;

// 查询教练列表的请求参数（支持筛选）
@Data
public class CoachListRequest {
    private Integer storeId;
    private String name;          // 教练姓名模糊搜索
    private String gender;        // 性别筛选
    private Boolean isAvailable;  // 是否只看有空闲的
    private LocalDate date;       // 查询哪一天的空闲时段

    // Getters and Setters
}