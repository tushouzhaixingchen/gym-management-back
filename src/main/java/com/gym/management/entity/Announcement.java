package com.gym.management.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "announcements")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "store_id")
    private Integer storeId; // NULL 为全系统公告

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "publish_type", length = 20)
    private String publishType = "all"; // all/single

    @Column(name = "priority")
    private Integer priority = 1; // 1 普通 2 重要 3 紧急

    @Column(name = "publish_status")
    private Integer publishStatus = 0; // 0 草稿 1 已发布 2 已下架

    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "author_id")
    private Integer authorId;

    @Column(name = "author_name", length = 50)
    private String authorName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", insertable = false, updatable = false)
    private Store store;
}