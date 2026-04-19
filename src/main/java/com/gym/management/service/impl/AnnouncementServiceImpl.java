package com.gym.management.service.impl;

import com.gym.management.dto.request.admin.AnnouncementCreateRequest;
import com.gym.management.dto.request.admin.AnnouncementQueryRequest;
import com.gym.management.dto.request.admin.AnnouncementUpdateRequest;
import com.gym.management.dto.response.AnnouncementResponse;
import com.gym.management.entity.Announcement;
import com.gym.management.entity.Store;
import com.gym.management.entity.Admin;
import com.gym.management.common.exception.BusinessException;
import com.gym.management.repository.AnnouncementRepository;
import com.gym.management.repository.StoreRepository;
import com.gym.management.repository.AdminRepository;
import com.gym.management.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AdminRepository adminRepository;

    // ================= 辅助方法：获取当前登录管理员信息 =================

    /**
     * 获取当前登录管理员的 ID（从 SecurityContext）
     */
    private Integer getCurrentAdminId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (principal instanceof Integer) {
            return (Integer) principal;
        } else {
            log.warn("【权限检查】Principal 不是 Integer 类型：{}", principal.getClass().getName());
            return null;
        }
    }

    /**
     * 获取当前登录管理员的信息
     * @return Admin 对象，如果未找到返回 null
     */
    private Admin getCurrentAdmin() {
        Integer adminId = getCurrentAdminId();
        if (adminId == null) return null;
        return adminRepository.findById(adminId).orElse(null);
    }

    /**
     * 检查是否为超级管理员（roleId=1）
     */
    private boolean isSuperAdmin(Admin admin) {
        if (admin == null) return false;
        return admin.getRoleId() != null && admin.getRoleId() == 1;
    }

    // ================= 查询 =================

    @Override
    public Page<AnnouncementResponse> queryAnnouncements(AnnouncementQueryRequest request) {
        // 🔴 权限检查：获取当前登录管理员信息
        Admin currentAdmin = getCurrentAdmin();
        boolean isSuperAdmin = isSuperAdmin(currentAdmin);
        Integer currentStoreId = currentAdmin != null ? currentAdmin.getStoreId() : null;
        
        log.info("【权限检查】公告列表查询 - 管理员 ID: {}, 用户名：{}, 角色 ID: {}, 是否超级管理员：{}", 
            currentAdmin != null ? currentAdmin.getId() : "null",
            currentAdmin != null ? currentAdmin.getUsername() : "null",
            currentAdmin != null ? currentAdmin.getRoleId() : "null",
            isSuperAdmin);
        
        // 确定最终使用的门店 ID（用于筛选单门店公告）
        final Integer effectiveStoreId;
        
        if (isSuperAdmin) {
            // 超级管理员可以查看所有公告（包括全系统和本店）
            effectiveStoreId = request.getStoreId();
            if (effectiveStoreId == null) {
                log.info("【权限检查】✓ 超级管理员模式：查询所有门店 + 全系统的公告");
            } else {
                log.info("【权限检查】✓ 超级管理员模式：查询指定门店 ID={} + 全系统的公告", effectiveStoreId);
            }
        } else if (currentStoreId != null) {
            // 门店管理员只能查看本店公告 + 全系统公告
            effectiveStoreId = currentStoreId;
            log.info("【权限检查】✓ 门店管理员模式：只能查询本店 ID={} + 全系统的公告", effectiveStoreId);
        } else {
            effectiveStoreId = null;
            log.warn("【权限检查】✗ 当前用户既不是超级管理员也没有门店 ID，无权访问");
        }
        
        int page = Math.max(0, request.getPage() - 1);
        int size = Math.min(request.getSize(), 100);
        Sort sort = Sort.by(Sort.Direction.DESC, "priority", "publishTime");
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<Announcement> spec = (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();

            // 🔴 添加门店权限过滤
            if (effectiveStoreId != null) {
                // 门店管理员模式：查询本店公告 OR 全系统公告（storeId IS NULL）
                var storePredicate = cb.or(
                    cb.equal(root.get("storeId"), effectiveStoreId),
                    cb.isNull(root.get("storeId"))
                );
                predicates.add(storePredicate);
                log.debug("【权限过滤】添加门店条件：storeId={} OR storeId IS NULL", effectiveStoreId);
            } else {
                // 超级管理员查询所有：不添加门店限制
                log.debug("【权限过滤】超级管理员模式，不添加门店限制");
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

        // 检查门店权限 (storeId 为 null 时会员可以查看所有公告，不做门店隔离)
        if (storeId != null && announcement.getStoreId() != null && !announcement.getStoreId().equals(storeId)) {
            throw new BusinessException("无权查看该门店公告");
        }

        // 增加浏览次数
        incrementViewCount(id);

        String storeName = fetchStoreName(announcement.getStoreId());
        return AnnouncementResponse.fromEntity(announcement, storeName);
    }

    @Override
    public Page<AnnouncementResponse> getPublishedAnnouncements(Integer storeId, Integer page, Integer size) {
        log.info("【公告查询】会员端公告列表查询 - storeId: {}, page: {}, size: {}", storeId, page, size);
        
        int pageNum = Math.max(0, page - 1);
        int pageSize = Math.min(size, 100);
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize);

        Page<Announcement> pageData;
        if (storeId != null) {
            log.info("【公告查询】使用门店过滤查询 - storeId: {}", storeId);
            pageData = announcementRepository.findAllPublishedAnnouncementsByStore(storeId, pageRequest);
        } else {
            log.info("【公告查询】使用全量查询（无门店过滤，包含已过期）");
            pageData = announcementRepository.findAllPublishedAnnouncements(pageRequest);
        }
        
        log.info("【公告查询】数据库返回结果 - 总数: {}, 当前页数量: {}", pageData.getTotalElements(), pageData.getContent().size());
        pageData.getContent().forEach(a -> 
            log.info("【公告查询】公告 ID: {}, 标题: {}, storeId: {}, publishStatus: {}, expireTime: {}", 
                a.getId(), a.getTitle(), a.getStoreId(), a.getPublishStatus(), a.getExpireTime())
        );

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