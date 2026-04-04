package com.gym.management.service.impl;

import com.gym.management.common.exception.BusinessException;
import com.gym.management.dto.response.PageResult;
import com.gym.management.dto.response.StoreResponse;
import com.gym.management.entity.Store;
import com.gym.management.repository.StoreRepository;
import com.gym.management.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 门店服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;

    @Override
    @Transactional(readOnly = true)
    public StoreResponse getStoreDetail(Integer id) {
        log.info("获取门店详情 | 门店ID: {}", id);
        
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("门店不存在 | 门店ID: {}", id);
                    return new BusinessException("门店不存在");
                });
        
        StoreResponse response = StoreResponse.fromEntity(store);
        log.info("获取门店详情成功 | 门店ID: {}, 门店名称: {}", id, store.getStoreName());
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<StoreResponse> getStoreList(Integer page, Integer size, Integer status, String keyword) {
        log.info("获取门店列表 | 页码: {}, 每页数量: {}, 状态: {}, 关键词: {}", page, size, status, keyword);
        
        // 参数校验和默认值处理
        int pageNum = Math.max(1, page != null ? page : 1);
        int pageSize = Math.min(Math.max(1, size != null ? size : 10), 100); // 限制最大100条
        
        // 构建分页请求（Spring Data JPA 页码从0开始）
        PageRequest pageRequest = PageRequest.of(
                pageNum - 1, 
                pageSize, 
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        
        // 根据条件查询
        Specification<Store> spec = (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likePattern = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("storeName"), likePattern),
                        cb.like(root.get("storeCode"), likePattern)
                ));
            }
            
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        
        Page<Store> storePage = storeRepository.findAll(spec, pageRequest);
        
        // 转换为响应对象
        List<StoreResponse> responseList = storePage.getContent().stream()
                .map(StoreResponse::fromEntity)
                .collect(Collectors.toList());
        
        PageResult<StoreResponse> result = PageResult.of(
                responseList,
                storePage.getTotalElements(),
                (long) pageNum,
                (long) pageSize
        );
        
        log.info("获取门店列表成功 | 总数: {}, 当前页: {}", storePage.getTotalElements(), pageNum);
        
        return result;
    }
}
