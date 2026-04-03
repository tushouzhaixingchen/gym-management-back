package com.gym.management.controller.admin;

import com.gym.management.dto.request.admin.*;
import com.gym.management.dto.response.CourseResponse;
import com.gym.management.dto.response.CourseBookResponse;
import com.gym.management.dto.response.PageResult;
import com.gym.management.common.Result;
import com.gym.management.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 课程管理控制器
 * ⚠️ 所有接口仅限 ADMIN 角色访问
 */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // ================= 课程管理接口 =================

    /**
     * 获取课程列表
     * GET /api/courses?page=1&size=10&storeId=1&courseType=瑜伽&status=1
     * 🔐 权限：仅 ADMIN
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<PageResult<CourseResponse>>> queryCourses(
            @ModelAttribute @Validated CourseQueryRequest request) {

        Page<CourseResponse> page = courseService.queryCourses(request);
        PageResult<CourseResponse> pageResult = PageResult.of(
                page.getContent(),
                page.getTotalElements(),
                (long) page.getNumber() + 1,
                (long) page.getSize()
        );
        return ResponseEntity.ok(Result.success(pageResult));
    }

    /**
     * 获取课程详情
     * GET /api/courses/{id}
     * 🔐 权限：仅 ADMIN
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<CourseResponse>> getCourseDetail(@PathVariable Integer id) {
        CourseResponse response = courseService.getCourseDetail(id);
        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * 新增课程
     * POST /api/courses
     * 🔐 权限：仅 ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<CourseResponse>> createCourse(
            @RequestBody @Validated CourseCreateRequest request) {
        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(response, "课程创建成功"));
    }

    /**
     * 编辑课程
     * PUT /api/courses/{id}
     * 🔐 权限：仅 ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<CourseResponse>> updateCourse(
            @PathVariable Integer id,
            @RequestBody @Validated CourseUpdateRequest request) {
        CourseResponse response = courseService.updateCourse(id, request);
        return ResponseEntity.ok(Result.success(response, "课程信息更新成功"));
    }

    /**
     * 删除课程
     * DELETE /api/courses/{id}
     * 🔐 权限：仅 ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> deleteCourse(@PathVariable Integer id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(Result.success(null, "课程已取消"));
    }

    // ================= 课程状态管理接口 =================

    /**
     * 开始课程
     * POST /api/courses/{id}/start
     * 🔐 权限：仅 ADMIN
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<CourseResponse>> startCourse(@PathVariable Integer id) {
        CourseResponse response = courseService.startCourse(id);
        return ResponseEntity.ok(Result.success(response, "课程已开始"));
    }

    /**
     * 结束课程
     * POST /api/courses/{id}/finish
     * 🔐 权限：仅 ADMIN
     */
    @PostMapping("/{id}/finish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<CourseResponse>> finishCourse(@PathVariable Integer id) {
        CourseResponse response = courseService.finishCourse(id);
        return ResponseEntity.ok(Result.success(response, "课程已结束"));
    }

    /**
     * 取消课程
     * POST /api/courses/{id}/cancel
     * 🔐 权限：仅 ADMIN
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<CourseResponse>> cancelCourse(@PathVariable Integer id) {
        CourseResponse response = courseService.cancelCourse(id);
        return ResponseEntity.ok(Result.success(response, "课程已取消"));
    }

    // ================= 课程预约管理接口 =================

    /**
     * 预约课程
     * POST /api/courses/book
     * 🔐 权限：仅 ADMIN
     */
    @PostMapping("/book")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<CourseBookResponse>> bookCourse(
            @RequestBody @Validated CourseBookRequest request) {
        CourseBookResponse response = courseService.bookCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(response, "预约成功"));
    }

    /**
     * 取消预约
     * POST /api/courses/cancel-book
     * 🔐 权限：仅 ADMIN
     */
    @PostMapping("/cancel-book")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<CourseBookResponse>> cancelBook(
            @RequestBody @Validated CourseCancelBookRequest request) {
        CourseBookResponse response = courseService.cancelBook(request);
        return ResponseEntity.ok(Result.success(response, "预约已取消"));
    }

    /**
     * 获取课程预约记录列表
     * GET /api/courses/{courseId}/books?page=1&size=10
     * 🔐 权限：仅 ADMIN
     */
    @GetMapping("/{courseId}/books")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<PageResult<CourseBookResponse>>> queryCourseBooks(
            @PathVariable Integer courseId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<CourseBookResponse> pageData = courseService.queryCourseBooks(courseId, page, size);
        PageResult<CourseBookResponse> pageResult = PageResult.of(
                pageData.getContent(),
                pageData.getTotalElements(),
                (long) pageData.getNumber() + 1,
                (long) pageData.getSize()
        );
        return ResponseEntity.ok(Result.success(pageResult));
    }

    // ================= 课程统计接口 =================

    /**
     * 获取课程剩余座位数
     * GET /api/courses/{id}/available-seats
     * 🔐 权限：仅 ADMIN
     */
    @GetMapping("/{id}/available-seats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Integer>> getAvailableSeats(@PathVariable Integer id) {
        int availableSeats = courseService.getAvailableSeats(id);
        return ResponseEntity.ok(Result.success(availableSeats));
    }

    /**
     * 检查课程是否已满
     * GET /api/courses/{id}/is-full
     * 🔐 权限：仅 ADMIN
     */
    @GetMapping("/{id}/is-full")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Boolean>> isCourseFull(@PathVariable Integer id) {
        boolean isFull = courseService.isCourseFull(id);
        return ResponseEntity.ok(Result.success(isFull));
    }
}