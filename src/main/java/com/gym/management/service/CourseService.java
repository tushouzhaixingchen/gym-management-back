package com.gym.management.service;

import com.gym.management.dto.request.admin.*;
import com.gym.management.dto.request.member.CourseBookingRequest;
import com.gym.management.dto.request.member.PaymentRequest;
import com.gym.management.dto.response.CourseResponse;
import com.gym.management.dto.response.CourseBookResponse;
import com.gym.management.dto.response.CourseBookingResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CourseService {

    // ========== 课程管理 ==========
    Page<CourseResponse> queryCourses(CourseQueryRequest request);

    CourseResponse getCourseDetail(Integer id);

    CourseResponse createCourse(CourseCreateRequest request);

    CourseResponse updateCourse(Integer id, CourseUpdateRequest request);

    void deleteCourse(Integer id);

    // ========== 课程预约管理 ==========
    CourseBookResponse bookCourse(CourseBookRequest request);

    CourseBookResponse cancelBook(CourseCancelBookRequest request);

    Page<CourseBookResponse> queryCourseBooks(Integer courseId, Integer page, Integer size);

    // ========== 课程状态管理 ==========
    CourseResponse startCourse(Integer id);

    CourseResponse finishCourse(Integer id);

    CourseResponse cancelCourse(Integer id);

    // ========== 统计 ==========
    int getAvailableSeats(Integer courseId);

    boolean isCourseFull(Integer courseId);

    // ========== 会员端课程报名 ==========
    /**
     * 查询可报名的课程列表（会员端）
     */
    Page<CourseResponse> queryCoursesForMember(Integer page, Integer size, Integer storeId);

    /**
     * 会员报名课程
     */
    CourseBookingResponse memberBookCourse(CourseBookingRequest request, Integer memberId);

    /**
     * 获取我的课程报名列表
     */
    List<CourseBookingResponse> getMyCourseBookings(Integer memberId);

    /**
     * 支付课程报名
     */
    CourseBookingResponse payCourseBooking(Integer bookingId, PaymentRequest request);
}