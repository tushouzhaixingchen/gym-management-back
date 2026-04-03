package com.gym.management.repository;

import com.gym.management.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer>, JpaSpecificationExecutor<Course> {

    /** 查询某门店的课程 */
    Page<Course> findByStoreId(Integer storeId, Pageable pageable);

    /** 查询某教练的课程 */
    Page<Course> findByCoachId(Integer coachId, Pageable pageable);

    /** 查询某时间段内的课程 */
    @Query("SELECT c FROM Course c WHERE c.storeId = :storeId AND c.startTime BETWEEN :start AND :end ORDER BY c.startTime ASC")
    List<Course> findByStoreIdAndTimeRange(
            @Param("storeId") Integer storeId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /** 查询可预约的课程 */
    Page<Course> findByStoreIdAndStatus(Integer storeId, Integer status, Pageable pageable);

    /** 统计某教练的课程数量 */
    long countByCoachId(Integer coachId);

    /** 统计某门店的课程数量 */
    long countByStoreId(Integer storeId);

    /** 查询即将开始的课程 */
    @Query("SELECT c FROM Course c WHERE c.status = 1 AND c.startTime BETWEEN :now AND :future ORDER BY c.startTime ASC")
    List<Course> findUpcomingCourses(
            @Param("now") LocalDateTime now,
            @Param("future") LocalDateTime future);
}