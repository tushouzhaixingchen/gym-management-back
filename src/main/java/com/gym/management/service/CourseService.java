package com.gym.management.service;

import com.gym.management.dto.request.admin.*;
import com.gym.management.dto.response.CourseResponse;
import com.gym.management.dto.response.CourseBookResponse;
import org.springframework.data.domain.Page;

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
}