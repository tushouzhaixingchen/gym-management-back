package com.gym.management.dto.response;

import com.gym.management.entity.Announcement;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementResponse {

    private Integer id;
    private Integer storeId;
    private String storeName;
    private String title;
    private String content;
    private String publishType;
    private String publishTypeText;
    private Integer priority;
    private String priorityText;
    private Integer publishStatus;
    private String publishStatusText;
    private LocalDateTime publishTime;
    private LocalDateTime expireTime;
    private Integer viewCount;
    private Integer authorId;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isExpired; // 是否已过期

    public static AnnouncementResponse fromEntity(Announcement announcement, String storeName) {
        return AnnouncementResponse.builder()
                .id(announcement.getId())
                .storeId(announcement.getStoreId())
                .storeName(storeName)
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .publishType(announcement.getPublishType())
                .publishTypeText(getPublishTypeText(announcement.getPublishType()))
                .priority(announcement.getPriority())
                .priorityText(getPriorityText(announcement.getPriority()))
                .publishStatus(announcement.getPublishStatus())
                .publishStatusText(getPublishStatusText(announcement.getPublishStatus()))
                .publishTime(announcement.getPublishTime())
                .expireTime(announcement.getExpireTime())
                .viewCount(announcement.getViewCount())
                .authorId(announcement.getAuthorId())
                .authorName(announcement.getAuthorName())
                .createdAt(announcement.getCreatedAt())
                .updatedAt(announcement.getUpdatedAt())
                .isExpired(checkIsExpired(announcement.getExpireTime()))
                .build();
    }

    private static String getPublishTypeText(String publishType) {
        if (publishType == null) return "未知";
        return switch (publishType) {
            case "all" -> "全系统";
            case "single" -> "单门店";
            default -> publishType;
        };
    }

    private static String getPriorityText(Integer priority) {
        if (priority == null) return "未知";
        return switch (priority) {
            case 1 -> "普通";
            case 2 -> "重要";
            case 3 -> "紧急";
            default -> "未知";
        };
    }

    private static String getPublishStatusText(Integer publishStatus) {
        if (publishStatus == null) return "未知";
        return switch (publishStatus) {
            case 0 -> "草稿";
            case 1 -> "已发布";
            case 2 -> "已下架";
            default -> "未知";
        };
    }

    private static Boolean checkIsExpired(LocalDateTime expireTime) {
        if (expireTime == null) return false;
        return LocalDateTime.now().isAfter(expireTime);
    }
}