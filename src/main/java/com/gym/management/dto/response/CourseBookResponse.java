package com.gym.management.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseBookResponse {

    private Integer id;
    private Integer courseId;
    private String courseName;
    private Integer memberId;
    private String memberName;
    private Integer status; // 1 已预约 2 已取消 3 已完成
    private String statusText;
    private LocalDateTime bookTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private String remark;

    private static String getStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "已预约";
            case 2 -> "已取消";
            case 3 -> "已完成";
            default -> "未知";
        };
    }
}