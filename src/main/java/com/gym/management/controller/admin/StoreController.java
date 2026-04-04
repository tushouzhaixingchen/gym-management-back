package com.gym.management.controller.admin;

import com.gym.management.common.Result;
import com.gym.management.dto.response.PageResult;
import com.gym.management.dto.response.StoreResponse;
import com.gym.management.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 门店管理控制器
 * 🔐 权限：所有接口仅限 ADMIN 角色访问
 */
@Slf4j
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    /**
     * 获取门店列表（分页）
     * GET /api/stores?page=1&size=10&status=1&keyword=北京
     * 🔐 权限：仅 ADMIN
     * 
     * @param page 页码（从1开始，默认1）
     * @param size 每页数量（默认10，最大100）
     * @param status 状态筛选（可选）：0-停业，1-营业中，2-装修中
     * @param keyword 关键词搜索（可选）：门店名称、门店编码
     * @return 分页的门店列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<PageResult<StoreResponse>>> getStoreList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        
        log.info("查询门店列表 | 页码: {}, 每页数量: {}, 状态: {}, 关键词: {}", page, size, status, keyword);
        
        PageResult<StoreResponse> result = storeService.getStoreList(page, size, status, keyword);
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 获取门店详情
     * GET /api/stores/{id}
     * 🔐 权限：仅 ADMIN
     * 
     * @param id 门店ID
     * @return 门店详细信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<StoreResponse>> getStoreDetail(@PathVariable Integer id) {
        log.info("查询门店详情 | 门店ID: {}", id);
        
        StoreResponse response = storeService.getStoreDetail(id);
        return ResponseEntity.ok(Result.success(response));
    }
}
