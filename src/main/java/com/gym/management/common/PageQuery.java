package com.gym.management.common;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
public class PageQuery {

    /** 当前页码 (从1开始) */
    private Integer page = 1;

    /** 每页数量 */
    private Integer size = 10;

    /** 排序字段 (可选，例如 "createTime,desc") */
    private String sort;

    /** 排序方向: asc 或 desc (可选) */
    private String order = "desc";

    /**
     * 转换为 Spring Data JPA 的 Pageable 对象
     * 包含统一的边界检查和默认逻辑
     */
    public Pageable toPageable() {
        // 1. 页码修正 (前端传1，后端从0开始)
        int pageNum = Math.max(0, this.page - 1);

        // 2. 页数限制 (防止有人传 size=99999 搞垮数据库)
        int pageSize = Math.min(this.size != null ? this.size : 10, 100);

        // 3. 处理排序
        if (this.sort != null && !this.sort.isEmpty()) {
            Sort.Direction direction = "asc".equalsIgnoreCase(this.order)
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            return PageRequest.of(pageNum, pageSize, Sort.by(direction, this.sort));
        }

        // 4. 默认无排序 (或由 Service 层指定默认排序)
        return PageRequest.of(pageNum, pageSize);
    }
}