package com.gym.management.controller.member;

import com.gym.management.common.Result;
import com.gym.management.common.ResultCode;
import com.gym.management.common.exception.BusinessException;
import com.gym.management.dto.request.member.ChangePasswordRequest;
import com.gym.management.dto.request.member.MemberProfileUpdateRequest;
import com.gym.management.dto.response.AnnouncementResponse;
import com.gym.management.dto.response.AppointmentResponse;
import com.gym.management.dto.response.MemberProfileVO;
import com.gym.management.dto.response.OrderResponse;
import com.gym.management.service.AnnouncementService;
import com.gym.management.service.AppointmentService;
import com.gym.management.service.MemberService;
import com.gym.management.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员中心 - 我的接口
 * 处理会员个人信息相关的接口（我的预约、我的订单等）
 */
@Slf4j
@RestController
@RequestMapping("/api/member")
public class MemberPersonalController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private AnnouncementService announcementService;

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

    /**
     * 获取我的预约列表
     */
    @GetMapping("/appointments/my")
    @PreAuthorize("hasRole('MEMBER')")
    public List<AppointmentResponse> getMyAppointments() {
        Integer memberId = getCurrentMemberId();
        return appointmentService.getMyAppointments(memberId);
    }

    /**
     * 获取我的订单列表
     * @return 当前会员的订单列表，按创建时间倒序排列
     */
    @GetMapping("/orders/my")
    @PreAuthorize("hasRole('MEMBER')")
    public List<OrderResponse> getMyOrders() {
        Integer memberId = getCurrentMemberId();
        return orderService.getMemberOrders(memberId);
    }

    /**
     * 获取我的个人信息
     * @return 当前会员的完整个人信息
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('MEMBER')")
    public MemberProfileVO getMyProfile() {
        Integer memberId = getCurrentMemberId();
        return memberService.getMemberProfile(memberId);
    }

    /**
     * 修改个人信息
     * 会员只能修改自己的基础信息（姓名、性别、邮箱、生日、备注）
     * @param request 更新请求
     * @return 更新后的会员个人信息
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('MEMBER')")
    public MemberProfileVO updateMyProfile(@RequestBody MemberProfileUpdateRequest request) {
        Integer memberId = getCurrentMemberId();
        return memberService.updateMemberProfile(memberId, request);
    }

    /**
     * 修改密码
     * 验证旧密码，更新为新密码，并将 is_initial_password 设置为 2
     * @param request 修改密码请求
     */
    @PutMapping("/security/password")
    @PreAuthorize("hasRole('MEMBER')")
    public void changePassword(@RequestBody ChangePasswordRequest request) {
        Integer memberId = getCurrentMemberId();
        memberService.changePassword(memberId, request);
    }

    /**
     * 退出登录
     * JWT是无状态认证，前端收到响应后清除本地Token即可
     * 注意：此接口不会让Token在服务端失效，仅返回成功响应
     * @return 操作成功
     */
    @PostMapping("/auth/logout")
    @PreAuthorize("hasRole('MEMBER')")
    public void logout() {
        Integer memberId = getCurrentMemberId();
        log.info("会员 {} 请求退出登录，前端需清除Token", memberId);
        // JWT无状态，前端删除本地Token即可，后续请求将因无Token而被拦截
    }

    /**
     * 获取公告列表（会员端）
     * 会员可以查看所有已发布的公告（全系统公告 + 所有门店公告）
     * GET /api/member/announcements?page=1&size=10
     * 🔐 权限：MEMBER
     */
    @GetMapping("/announcements")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<Result<com.gym.management.common.PageResult<AnnouncementResponse>>> getAnnouncementList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        // 会员可以查看所有公告，传入 null 不做门店隔离
        Page<AnnouncementResponse> pageData = announcementService.getPublishedAnnouncements(null, page, size);
        com.gym.management.common.PageResult<AnnouncementResponse> pageResult = com.gym.management.common.PageResult.of(
                pageData.getContent(),
                pageData.getTotalElements(),
                (long) pageData.getNumber() + 1,
                (long) pageData.getSize()
        );
        return ResponseEntity.ok(Result.success(pageResult));
    }

    /**
     * 获取公告详情（会员端）
     * 会员可以查看任何已发布且未过期的公告
     * GET /api/member/announcements/{id}
     * 🔐 权限：MEMBER
     */
    @GetMapping("/announcements/{id}")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<Result<AnnouncementResponse>> getAnnouncementDetail(@PathVariable Integer id) {
        // 会员可以查看所有公告，传入 null 不做门店隔离
        AnnouncementResponse response = announcementService.getAnnouncementDetailForMember(id, null);
        return ResponseEntity.ok(Result.success(response));
    }
}
