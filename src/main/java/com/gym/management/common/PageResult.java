// dto/response/PageResult.java（通用分页）
package com.gym.management.common;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private List<T> list;
    private Long total;
    private Long page;
    private Long size;

    public static <T> PageResult<T> of(List<T> list, Long total, Long page, Long size) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);
        return result;
    }
}