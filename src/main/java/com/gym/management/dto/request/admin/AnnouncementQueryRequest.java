package com.gym.management.dto.request.admin;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Data
public class AnnouncementQueryRequest {

    @Min(value = 1, message = "页码最小为 1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量最小为 1")
    @Max(value = 100, message = "每页数量最大为 100")
    private Integer size = 10;

    private Integer storeId; // 门店 ID，null 表示查询全系统公告
    private Integer publishStatus; // 0 草稿 1 已发布 2 已下架
    private String publishType; // all/single
    private Integer priority; // 1 普通 2 重要 3 紧急
}