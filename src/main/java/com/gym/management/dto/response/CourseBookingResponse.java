package com.gym.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员课程报名响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseBookingResponse {

    private Integer id;
    private String bookingNo;
    private Integer courseId;
    private String courseName;
    private String courseType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String room;
    private Integer storeId;
    private String storeName;
    private Integer coachId;
    private String coachName;
    private BigDecimal price;
    private Integer status; // 0已报名 1已结束
    private String statusText;
    private Integer payStatus; // 0未支付 1已支付
    private String payStatusText;
    private LocalDateTime payTime;
    private Integer payMethod; // 1微信 2支付宝 3现金
    private String payMethodText;
    private LocalDateTime memberCheckInTime;
    private LocalDateTime memberCheckOutTime;
    private Integer feedbackScore;
    private String remark;
    private LocalDateTime createdAt;

    public static String getStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "已报名";
            case 1 -> "已结束";
            default -> "未知";
        };
    }

    public static String getPayStatusText(Integer payStatus) {
        if (payStatus == null) return "未知";
        return switch (payStatus) {
            case 0 -> "未支付";
            case 1 -> "已支付";
            default -> "未知";
        };
    }

    public static String getPayMethodText(Integer payMethod) {
        if (payMethod == null) return "未知";
        return switch (payMethod) {
            case 1 -> "微信";
            case 2 -> "支付宝";
            case 3 -> "现金";
            default -> "未知";
        };
    }
}
