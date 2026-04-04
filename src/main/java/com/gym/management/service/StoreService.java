package com.gym.management.service;

import com.gym.management.dto.response.PageResult;
import com.gym.management.dto.response.StoreResponse;

/**
 * 门店服务接口
 */
public interface StoreService {

    /**
     * 获取门店详情
     * @param id 门店ID
     * @return 门店详细信息
     */
    StoreResponse getStoreDetail(Integer id);

    /**
     * 获取门店列表（分页）
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @param status 状态筛选（可选）：0-停业，1-营业中，2-装修中
     * @param keyword 关键词搜索（可选）：门店名称、门店编码
     * @return 分页结果
     */
    PageResult<StoreResponse> getStoreList(Integer page, Integer size, Integer status, String keyword);
}
