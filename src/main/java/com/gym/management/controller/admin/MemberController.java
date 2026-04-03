// src/main/java/com/gym/management/controller/MemberController.java

package com.gym.management.controller.admin;

import com.gym.management.dto.request.admin.*;
import com.gym.management.dto.response.MemberPageResponse;
import com.gym.management.dto.response.MemberResponse;
import com.gym.management.dto.response.MemberVisitResponse;
import com.gym.management.service.MemberService;
import com.gym.management.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // 🔥 权限控制注解
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 会员管理控制器
 * ⚠️ 严格模式：所有接口仅限 ADMIN 角色访问
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // ================= 查询接口 =================

    /**
     * 获取会员列表
     * 🔐 权限：仅 ADMIN
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<MemberPageResponse<MemberResponse>>> getMemberList(
            @ModelAttribute @Validated MemberQueryRequest request) {

        Page<MemberResponse> pageData = memberService.queryMembers(request);
        MemberPageResponse<MemberResponse> resultBody = MemberPageResponse.fromPage(pageData);
        return ResponseEntity.ok(Result.success(resultBody));
    }

    /**
     * 获取会员详情
     * 🔐 权限：仅 ADMIN
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<MemberResponse>> getMemberDetail(@PathVariable Integer id) {
        MemberResponse data = memberService.getMemberDetail(id);
        return ResponseEntity.ok(Result.success(data));
    }

    // ================= 基础维护接口 =================

    /**
     * 新增会员
     * 🔐 权限：仅 ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<MemberResponse>> createMember(
            @RequestBody @Validated MemberCreateRequest request) {
        MemberResponse data = memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(data));
    }

    /**
     * 编辑会员信息
     * 🔐 权限：仅 ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<MemberResponse>> updateMember(
            @PathVariable Integer id,
            @RequestBody @Validated MemberUpdateRequest request) {
        MemberResponse data = memberService.updateMember(id, request);
        return ResponseEntity.ok(Result.success(data));
    }

    /**
     * 删除/冻结会员
     * 🔐 权限：仅 ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> deleteMember(@PathVariable Integer id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(Result.success(null));
    }

    // ================= 核心业务接口 =================

    /**
     * 开卡 / 续费
     * 🔐 权限：仅 ADMIN
     */
    @PostMapping("/{id}/card")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<MemberResponse>> openOrRenewCard(
            @PathVariable Integer id,
            @RequestBody @Validated CardOperationRequest request) {
        MemberResponse data = memberService.openOrRenewCard(id, request);
        return ResponseEntity.ok(Result.success(data));
    }

    /**
     * 余额充值
     * 🔐 权限：仅 ADMIN
     */
    @PostMapping("/{id}/recharge")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<MemberResponse>> rechargeBalance(
            @PathVariable Integer id,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String remark) {
        MemberResponse data = memberService.rechargeBalance(id, amount, remark);
        return ResponseEntity.ok(Result.success(data));
    }

    /**
     * 调整次卡次数
     * 🔐 权限：仅 ADMIN
     */
    @PostMapping("/{id}/adjust-times")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<MemberResponse>> adjustTimes(
            @PathVariable Integer id,
            @RequestParam Integer times,
            @RequestParam String reason) {
        MemberResponse data = memberService.adjustTimes(id, times, reason);
        return ResponseEntity.ok(Result.success(data));
    }

    /**
     * 变更会员状态 (冻结/解冻)
     * 🔐 权限：仅 ADMIN
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<MemberResponse>> updateMemberStatus(
            @PathVariable Integer id,
            @RequestBody @Validated MemberStatusRequest request) {
        MemberResponse data = memberService.updateMemberStatus(id, request);
        return ResponseEntity.ok(Result.success(data));
    }

    /**
     * 会员签到
     * 🔐 权限：仅 ADMIN
     * (注意：如果前台需要签到，此接口前台将无法调用)
     */
    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<MemberVisitResponse>> memberCheckIn(
            @PathVariable Integer id,
            @RequestBody @Validated MemberVisitRequest request) {
        MemberVisitResponse data = memberService.memberVisit(id, request);
        return ResponseEntity.ok(Result.success(data));
    }

    // ================= 敏感操作接口 =================

    /**
     * 重置会员密码
     * 🔐 权限：仅 ADMIN
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<MemberResponse>> resetMemberPassword(@PathVariable Integer id) {
        MemberResponse data = memberService.resetMemberPassword(id);
        return ResponseEntity.ok(Result.success(data));
    }
}