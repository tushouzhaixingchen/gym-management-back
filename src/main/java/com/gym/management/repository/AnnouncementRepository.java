package com.gym.management.repository;

import com.gym.management.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Integer>, JpaSpecificationExecutor<Announcement> {

    /** 查询某门店的公告 */
    Page<Announcement> findByStoreId(Integer storeId, Pageable pageable);

    /** 查询全系统公告 (store_id IS NULL) */
    Page<Announcement> findByStoreIdIsNull(Pageable pageable);

    /** 查询某状态的公告 */
    Page<Announcement> findByPublishStatus(Integer publishStatus, Pageable pageable);

    /** 查询某门店某状态的公告 */
    Page<Announcement> findByStoreIdAndPublishStatus(Integer storeId, Integer publishStatus, Pageable pageable);

    /** 查询已发布的公告 (按优先级和发布时间排序) */
    @Query("SELECT a FROM Announcement a WHERE a.publishStatus = 1 AND (a.expireTime IS NULL OR a.expireTime > :now) ORDER BY a.priority DESC, a.publishTime DESC")
    Page<Announcement> findPublishedAnnouncements(@Param("now") LocalDateTime now, Pageable pageable);

    /** 查询某门店已发布的公告 */
    @Query("SELECT a FROM Announcement a WHERE a.publishStatus = 1 AND (a.storeId = :storeId OR a.storeId IS NULL) AND (a.expireTime IS NULL OR a.expireTime > :now) ORDER BY a.priority DESC, a.publishTime DESC")
    Page<Announcement> findPublishedAnnouncementsByStore(
            @Param("storeId") Integer storeId,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    /** 增加浏览次数 */
    @Modifying
    @Transactional
    @Query("UPDATE Announcement a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    int incrementViewCount(@Param("id") Integer id);

    /** 查询即将过期的公告 */
    @Query("SELECT a FROM Announcement a WHERE a.publishStatus = 1 AND a.expireTime IS NOT NULL AND a.expireTime BETWEEN :now AND :future ORDER BY a.expireTime ASC")
    List<Announcement> findExpiringAnnouncements(
            @Param("now") LocalDateTime now,
            @Param("future") LocalDateTime future);

    /** 统计某门店的公告数 */
    long countByStoreId(Integer storeId);

    /** 统计已发布的公告数 */
    long countByPublishStatus(Integer publishStatus);
}