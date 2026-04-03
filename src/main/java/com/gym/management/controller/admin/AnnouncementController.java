package com.gym.management.controller.admin;

import com.gym.management.dto.request.admin.AnnouncementCreateRequest;
import com.gym.management.dto.request.admin.AnnouncementQueryRequest;
import com.gym.management.dto.request.admin.AnnouncementUpdateRequest;
import com.gym.management.dto.response.AnnouncementResponse;
import com.gym.management.dto.response.PageResult;
import com.gym.management.common.Result;
import com.gym.management.service.AnnouncementService;
import com.gym.management.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端 - 公告管理控制器
 * 🔐 仅 ADMIN 角色可访问
 */
@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 获取公告列表
     * GET /api/admin/announcements?page=1&size=10&publishStatus=1
     * 🔐 权限：ADMIN
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<PageResult<AnnouncementResponse>>> queryAnnouncements(
            @ModelAttribute @Validated AnnouncementQueryRequest request) {

        Page<AnnouncementResponse> page = announcementService.queryAnnouncements(request);
        PageResult<AnnouncementResponse> pageResult = PageResult.of(
                page.getContent(),
                page.getTotalElements(),
                (long) page.getNumber() + 1,
                (long) page.getSize()
        );
        return ResponseEntity.ok(Result.success(pageResult));
    }

    /**
     * 获取公告详情
     * GET /api/admin/announcements/{id}
     * 🔐 权限：ADMIN
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<AnnouncementResponse>> getAnnouncementDetail(@PathVariable Integer id) {
        AnnouncementResponse response = announcementService.getAnnouncementDetail(id);
        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * 新增公告
     * POST /api/admin/announcements
     * 🔐 权限：ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<AnnouncementResponse>> createAnnouncement(
            @RequestBody @Validated AnnouncementCreateRequest request) {

        Integer authorId = UserContext.getCurrentUserId();
        String authorName = UserContext.getCurrentUserName();

        AnnouncementResponse response = announcementService.createAnnouncement(request, authorId, authorName);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(response, "公告创建成功"));
    }

    /**
     * 修改公告
     * PUT /api/admin/announcements/{id}
     * 🔐 权限：ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<AnnouncementResponse>> updateAnnouncement(
            @PathVariable Integer id,
            @RequestBody @Validated AnnouncementUpdateRequest request) {

        AnnouncementResponse response = announcementService.updateAnnouncement(id, request);
        return ResponseEntity.ok(Result.success(response, "公告信息更新成功"));
    }

    /**
     * 发布公告
     * POST /api/admin/announcements/{id}/publish
     * 🔐 权限：ADMIN
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> publishAnnouncement(@PathVariable Integer id) {
        announcementService.publishAnnouncement(id);
        return ResponseEntity.ok(Result.success(null, "公告已发布"));
    }

    /**
     * 下架公告
     * POST /api/admin/announcements/{id}/unpublish
     * 🔐 权限：ADMIN
     */
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> unpublishAnnouncement(@PathVariable Integer id) {
        announcementService.unpublishAnnouncement(id);
        return ResponseEntity.ok(Result.success(null, "公告已下架"));
    }

    /**
     * 删除公告
     * DELETE /api/admin/announcements/{id}
     * 🔐 权限：ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> deleteAnnouncement(@PathVariable Integer id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.ok(Result.success(null, "公告已删除"));
    }
}