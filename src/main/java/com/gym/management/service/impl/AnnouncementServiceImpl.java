package com.gym.management.service.impl;

import com.gym.management.dto.request.admin.AnnouncementCreateRequest;
import com.gym.management.dto.request.admin.AnnouncementQueryRequest;
import com.gym.management.dto.request.admin.AnnouncementUpdateRequest;
import com.gym.management.dto.response.AnnouncementResponse;
import com.gym.management.entity.Announcement;
import com.gym.management.entity.Store;
import com.gym.management.common.exception.BusinessException;
import com.gym.management.repository.AnnouncementRepository;
import com.gym.management.repository.StoreRepository;
import com.gym.management.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final StoreRepository storeRepository;

    // ================= 查询 =================

    @Override
    public Page<AnnouncementResponse> queryAnnouncements(AnnouncementQueryRequest request) {
        int page = Math.max(0, request.getPage() - 1);
        int size = Math.min(request.getSize(), 100);
        Sort sort = Sort.by(Sort.Direction.DESC, "priority", "publishTime");
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<Announcement> spec = (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();

            if (request.getStoreId() != null) {
                // 查询指定门店或全系统公告
                var storePredicate = cb.or(
                        cb.equal(root.get("storeId"), request.getStoreId()),
                        cb.isNull(root.get("storeId"))
                );
                predicates.add(storePredicate);
            }

            if (request.getPublishStatus() != null) {
                predicates.add(cb.equal(root.get("publishStatus"), request.getPublishStatus()));
            }

            if (StringUtils.hasText(request.getPublishType())) {
                predicates.add(cb.equal(root.get("publishType"), request.getPublishType()));
            }

            if (request.getPriority() != null) {
                predicates.add(cb.equal(root.get("priority"), request.getPriority()));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<Announcement> pageData = announcementRepository.findAll(spec, pageRequest);
        return pageData.map(announcement -> {
            String storeName = fetchStoreName(announcement.getStoreId());
            return AnnouncementResponse.fromEntity(announcement, storeName);
        });
    }

    @Override
    public AnnouncementResponse getAnnouncementDetail(Integer id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在，ID: " + id));
        String storeName = fetchStoreName(announcement.getStoreId());
        return AnnouncementResponse.fromEntity(announcement, storeName);
    }

    @Override
    public AnnouncementResponse getAnnouncementDetailForMember(Integer id, Integer storeId) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在，ID: " + id));

        // 会员只能查看已发布的公告
        if (announcement.getPublishStatus() != 1) {
            throw new BusinessException("该公告未发布，无法查看");
        }

        // 检查是否过期
        if (announcement.getExpireTime() != null && LocalDateTime.now().isAfter(announcement.getExpireTime())) {
            throw new BusinessException("该公告已过期");
        }

        // 检查门店权限 (单门店公告只能查看本门店的)
        if (announcement.getStoreId() != null && !announcement.getStoreId().equals(storeId)) {
            throw new BusinessException("无权查看该门店公告");
        }

        // 增加浏览次数
        incrementViewCount(id);

        String storeName = fetchStoreName(announcement.getStoreId());
        return AnnouncementResponse.fromEntity(announcement, storeName);
    }

    @Override
    public Page<AnnouncementResponse> getPublishedAnnouncements(Integer storeId, Integer page, Integer size) {
        int pageNum = Math.max(0, page - 1);
        int pageSize = Math.min(size, 100);
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize);
        LocalDateTime now = LocalDateTime.now();

        Page<Announcement> pageData;
        if (storeId != null) {
            pageData = announcementRepository.findPublishedAnnouncementsByStore(storeId, now, pageRequest);
        } else {
            pageData = announcementRepository.findPublishedAnnouncements(now, pageRequest);
        }

        return pageData.map(announcement -> {
            String storeName = fetchStoreName(announcement.getStoreId());
            return AnnouncementResponse.fromEntity(announcement, storeName);
        });
    }

    // ================= 管理 =================

    @Override
    @Transactional
    public AnnouncementResponse createAnnouncement(AnnouncementCreateRequest request, Integer authorId, String authorName) {
        // 验证时间
        if (request.getExpireTime() != null && request.getPublishTime() != null) {
            if (request.getExpireTime().isBefore(request.getPublishTime())) {
                throw new BusinessException("过期时间不能早于发布时间");
            }
        }

        // 如果状态为已发布，设置发布时间
        LocalDateTime publishTime = request.getPublishTime();
        if (request.getPublishStatus() == 1 && publishTime == null) {
            publishTime = LocalDateTime.now();
        }

        Announcement announcement = Announcement.builder()
                .storeId(request.getStoreId())
                .title(request.getTitle())
                .content(request.getContent())
                .publishType(request.getPublishType())
                .priority(request.getPriority())
                .publishStatus(request.getPublishStatus())
                .publishTime(publishTime)
                .expireTime(request.getExpireTime())
                .authorId(authorId)
                .authorName(authorName)
                .viewCount(0)
                .build();

        announcementRepository.save(announcement);

        String storeName = fetchStoreName(announcement.getStoreId());
        log.info("创建公告成功，ID: {}, 标题：{}, 发布人：{}", announcement.getId(), announcement.getTitle(), authorName);

        return AnnouncementResponse.fromEntity(announcement, storeName);
    }

    @Override
    @Transactional
    public AnnouncementResponse updateAnnouncement(Integer id, AnnouncementUpdateRequest request) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));

        // 已发布的公告修改需要谨慎
        if (announcement.getPublishStatus() == 1) {
            // 已发布的公告不能修改核心内容，只能修改状态或过期时间
            if (request.getTitle() != null || request.getContent() != null) {
                throw new BusinessException("已发布的公告不能修改标题和内容，请先下架");
            }
        }

        if (request.getStoreId() != null) announcement.setStoreId(request.getStoreId());
        if (StringUtils.hasText(request.getTitle())) announcement.setTitle(request.getTitle());
        if (StringUtils.hasText(request.getContent())) announcement.setContent(request.getContent());
        if (StringUtils.hasText(request.getPublishType())) announcement.setPublishType(request.getPublishType());
        if (request.getPriority() != null) announcement.setPriority(request.getPriority());
        if (request.getPublishStatus() != null) announcement.setPublishStatus(request.getPublishStatus());
        if (request.getPublishTime() != null) announcement.setPublishTime(request.getPublishTime());
        if (request.getExpireTime() != null) announcement.setExpireTime(request.getExpireTime());

        announcement.setUpdatedAt(LocalDateTime.now());
        announcementRepository.save(announcement);

        String storeName = fetchStoreName(announcement.getStoreId());
        return AnnouncementResponse.fromEntity(announcement, storeName);
    }

    @Override
    @Transactional
    public void publishAnnouncement(Integer id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));

        if (announcement.getPublishStatus() == 1) {
            throw new BusinessException("公告已发布");
        }

        if (announcement.getPublishStatus() == 2) {
            throw new BusinessException("已下架的公告不能发布，请重新创建");
        }

        announcement.setPublishStatus(1);
        announcement.setPublishTime(LocalDateTime.now());
        announcement.setUpdatedAt(LocalDateTime.now());
        announcementRepository.save(announcement);

        log.info("发布公告成功，ID: {}, 标题：{}", announcement.getId(), announcement.getTitle());
    }

    @Override
    @Transactional
    public void unpublishAnnouncement(Integer id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));

        if (announcement.getPublishStatus() != 1) {
            throw new BusinessException("只有已发布的公告才能下架");
        }

        announcement.setPublishStatus(2);
        announcement.setUpdatedAt(LocalDateTime.now());
        announcementRepository.save(announcement);

        log.info("下架公告成功，ID: {}, 标题：{}", announcement.getId(), announcement.getTitle());
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Integer id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));

        // 已发布的公告不能直接删除，需要先下架
        if (announcement.getPublishStatus() == 1) {
            throw new BusinessException("已发布的公告不能删除，请先下架");
        }

        announcementRepository.delete(announcement);
        log.info("删除公告成功，ID: {}, 标题：{}", id, announcement.getTitle());
    }

    // ================= 工具 =================

    @Override
    @Transactional
    public void incrementViewCount(Integer id) {
        announcementRepository.incrementViewCount(id);
    }

    @Override
    public boolean isExpired(Integer id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));

        if (announcement.getExpireTime() == null) return false;
        return LocalDateTime.now().isAfter(announcement.getExpireTime());
    }

    private String fetchStoreName(Integer storeId) {
        if (storeId == null) return "全系统";
        return storeRepository.findById(storeId)
                .map(Store::getStoreName)
                .orElse("未知门店");
    }
}