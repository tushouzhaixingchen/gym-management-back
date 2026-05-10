package com.gym.management.controller.member;

import com.gym.management.common.Result;
import com.gym.management.common.ResultCode;
import com.gym.management.common.exception.BusinessException;
import com.gym.management.dto.request.member.CourseBookingRequest;
import com.gym.management.dto.request.member.PaymentRequest;
import com.gym.management.dto.response.CourseBookingResponse;
import com.gym.management.dto.response.CourseResponse;
import com.gym.management.service.CourseService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员中心 - 课程报名接口
 * 🔐 所有接口仅限 MEMBER 角色访问
 */
@Slf4j
@RestController
@RequestMapping("/api/member/courses")
public class MemberCourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 获取可报名的课程列表
     * GET /api/member/courses?page=1&size=10&storeId=1
     * 🔐 权限：仅 MEMBER
     */
    @GetMapping
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<Result<Page<CourseResponse>>> getCourses(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer storeId) {
        log.info("查询可报名课程列表 | page: {}, size: {}, storeId: {}", page, size, storeId);
        
        // 这里可以调用 CourseService 查询可预约的课程
        // 暂时返回空页面，实际需要根据业务需求实现
        Page<CourseResponse> courses = courseService.queryCoursesForMember(page, size, storeId);
        return ResponseEntity.ok(Result.success(courses));
    }

    /**
     * 获取课程详情
     * GET /api/member/courses/{courseId}
     * 🔐 权限：仅 MEMBER
     */
    @GetMapping("/{courseId}")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<Result<CourseResponse>> getCourseDetail(@PathVariable Integer courseId) {
        log.info("查询课程详情 | courseId: {}", courseId);
        
        CourseResponse course = courseService.getCourseDetail(courseId);
        return ResponseEntity.ok(Result.success(course));
    }

    /**
     * 获取我的课程报名列表
     * GET /api/member/courses/bookings
     * 🔐 权限：仅 MEMBER
     */
    @GetMapping("/bookings")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<Result<List<CourseBookingResponse>>> getMyCourseBookings() {
        Integer memberId = getCurrentMemberId();
        log.info("查询会员课程报名列表 | memberId: {}", memberId);
        
        List<CourseBookingResponse> bookings = courseService.getMyCourseBookings(memberId);
        return ResponseEntity.ok(Result.success(bookings));
    }

    /**
     * 报名课程（报名即支付）
     * POST /api/member/courses/book
     * 🔐 权限：仅 MEMBER
     */
    @PostMapping("/book")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<Result<CourseBookingResponse>> bookCourse(
            @Valid @RequestBody CourseBookingRequest request) {
        Integer memberId = getCurrentMemberId();
        log.info("会员报名课程 | memberId: {}, courseId: {}", memberId, request.getCourseId());
        
        CourseBookingResponse response = courseService.memberBookCourse(request, memberId);
        return ResponseEntity.ok(Result.success(response, "报名并支付成功"));
    }

    /**
     * 获取当前登录会员的 ID
     * 从 JWT Token 解析并存入 SecurityContext 的用户 ID
     */
    private Integer getCurrentMemberId() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        
        if (principal instanceof Integer) {
            return (Integer) principal;
        } else {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }
}
