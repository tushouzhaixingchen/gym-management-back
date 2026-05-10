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
@Table(name = "course_bookings")
public class CourseBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "store_id", nullable = false)
    private Integer storeId;

    @Column(name = "booking_no", unique = true, nullable = false, length = 50)
    private String bookingNo;

    @Column(name = "member_id", nullable = false)
    private Integer memberId;

    @Column(name = "course_id", nullable = false)
    private Integer courseId;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "coach_share", precision = 10, scale = 2)
    private BigDecimal coachShare;

    @Column(name = "status")
    private Integer status = 0; // 0已报名 1已结束

    @Column(name = "pay_status")
    private Integer payStatus = 0; // 0未支付 1已支付

    @Column(name = "pay_time")
    private LocalDateTime payTime;

    @Column(name = "pay_method")
    private Integer payMethod; // 1微信 2支付宝 3现金

    @Column(name = "member_check_in_time")
    private LocalDateTime memberCheckInTime;

    @Column(name = "member_check_out_time")
    private LocalDateTime memberCheckOutTime;

    @Column(name = "actual_attend_minutes")
    private Integer actualAttendMinutes;

    @Column(name = "feedback_score")
    private Integer feedbackScore;

    @Column(name = "feedback_content", columnDefinition = "TEXT")
    private String feedbackContent;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", insertable = false, updatable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    private Member member;
}
