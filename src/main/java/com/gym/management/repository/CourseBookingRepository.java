package com.gym.management.repository;

import com.gym.management.entity.CourseBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseBookingRepository extends JpaRepository<CourseBooking, Integer> {

    /** 查询会员的课程报名记录 */
    List<CourseBooking> findByMemberIdOrderByCreatedAtDesc(Integer memberId);

    /** 分页查询会员的课程报名记录 */
    Page<CourseBooking> findByMemberIdOrderByCreatedAtDesc(Integer memberId, Pageable pageable);

    /** 查询某课程的报名记录 */
    Page<CourseBooking> findByCourseId(Integer courseId, Pageable pageable);

    /** 检查会员是否已报名某课程 */
    boolean existsByMemberIdAndCourseId(Integer memberId, Integer courseId);

    /** 查询某课程的已支付报名人数 */
    long countByCourseIdAndPayStatus(Integer courseId, Integer payStatus);

    /** 根据报名单号查询 */
    Optional<CourseBooking> findByBookingNo(String bookingNo);
}
