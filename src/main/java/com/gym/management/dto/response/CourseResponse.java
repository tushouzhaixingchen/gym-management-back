package com.gym.management.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {

    private Integer id;
    private Integer storeId;
    private String storeName;
    private Integer coachId;
    private String coachName;
    private String courseName;
    private String courseType;
    private String courseTypeText;
    private String courseLevel;
    private String courseLevelText;
    private Integer maxSeats;
    private Integer bookedSeats;
    private Integer availableSeats;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private BigDecimal price;
    private String room;
    private Integer status;
    private String statusText;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CourseResponse fromEntity(
            com.gym.management.entity.Course course,
            String storeName,
            String coachName) {
        return CourseResponse.builder()
                .id(course.getId())
                .storeId(course.getStoreId())
                .storeName(storeName)
                .coachId(course.getCoachId())
                .coachName(coachName)
                .courseName(course.getCourseName())
                .courseType(course.getCourseType())
                .courseTypeText(getCourseTypeText(course.getCourseType()))
                .courseLevel(course.getCourseLevel())
                .courseLevelText(getCourseLevelText(course.getCourseLevel()))
                .maxSeats(course.getMaxSeats())
                .bookedSeats(course.getBookedSeats())
                .availableSeats(course.getMaxSeats() - course.getBookedSeats())
                .startTime(course.getStartTime())
                .endTime(course.getEndTime())
                .durationMinutes(course.getDurationMinutes())
                .price(course.getPrice())
                .room(course.getRoom())
                .status(course.getStatus())
                .statusText(getStatusText(course.getStatus()))
                .remark(course.getRemark())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    private static String getCourseTypeText(String type) {
        if (type == null) return "未知";
        return switch (type) {
            case "瑜伽" -> "瑜伽课程";
            case "动感单车" -> "动感单车";
            case "搏击" -> "搏击课程";
            case "舞蹈" -> "舞蹈课程";
            case "力量" -> "力量训练";
            default -> type;
        };
    }

    private static String getCourseLevelText(String level) {
        if (level == null) return "未知";
        return switch (level) {
            case "beginner" -> "初级";
            case "intermediate" -> "中级";
            case "advanced" -> "高级";
            default -> level;
        };
    }

    public static String getStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "可预约";
            case 0 -> "已满";
            case 2 -> "已取消";
            case 3 -> "进行中";
            case 4 -> "已结束";
            default -> "未知";
        };
    }
}