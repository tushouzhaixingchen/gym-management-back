package com.gym.management.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "store_id", nullable = false)
    private Integer storeId;

    @Column(name = "coach_id")
    private Integer coachId;

    @Column(name = "course_name", nullable = false, length = 100)
    private String courseName;

    @Column(name = "course_type", nullable = false, length = 50)
    private String courseType; // 瑜伽/动感单车/搏击/舞蹈/力量

    @Column(name = "course_level", length = 20)
    private String courseLevel = "beginner"; // beginner/intermediate/advanced

    @Column(name = "max_seats")
    private Integer maxSeats = 20;

    @Column(name = "booked_seats")
    private Integer bookedSeats = 0;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "room", length = 50)
    private String room;

    @Column(name = "status")
    private Integer status = 1; // 1 可预约 0 已满 2 取消 3 进行中 4 已结束

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", insertable = false, updatable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", insertable = false, updatable = false)
    private Coach coach;
}