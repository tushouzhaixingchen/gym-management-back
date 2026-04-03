package com.gym.management.service;

import com.gym.management.dto.request.admin.AnnouncementCreateRequest;
import com.gym.management.dto.request.admin.AnnouncementQueryRequest;
import com.gym.management.dto.request.admin.AnnouncementUpdateRequest;
import com.gym.management.dto.response.AnnouncementResponse;
import org.springframework.data.domain.Page;

public interface AnnouncementService {

    // ========== 查询 ==========
    Page<AnnouncementResponse> queryAnnouncements(AnnouncementQueryRequest request);

    AnnouncementResponse getAnnouncementDetail(Integer id);

    AnnouncementResponse getAnnouncementDetailForMember(Integer id, Integer storeId);

    Page<AnnouncementResponse> getPublishedAnnouncements(Integer storeId, Integer page, Integer size);

    // ========== 管理 ==========
    AnnouncementResponse createAnnouncement(AnnouncementCreateRequest request, Integer authorId, String authorName);

    AnnouncementResponse updateAnnouncement(Integer id, AnnouncementUpdateRequest request);

    void publishAnnouncement(Integer id);

    void unpublishAnnouncement(Integer id);

    void deleteAnnouncement(Integer id);

    // ========== 工具 ==========
    void incrementViewCount(Integer id);

    boolean isExpired(Integer id);
}