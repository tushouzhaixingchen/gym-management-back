// src/main/java/com/gym/management/dto/response/MemberPageResponse.java

package com.gym.management.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 会员列表分页响应 DTO
 * 适配前端常见分页组件 (AntD, ElementUI) 的字段规范
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberPageResponse<T> {

    /**
     * 当前页的数据列表
     * 泛型化支持，虽然这里主要用 MemberResponse，但保持通用性更好
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码 (从 1 开始)
     */
    private Integer page;

    /**
     * 每页显示数量
     */
    private Integer size;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 静态构建方法：从 Spring Data Page 对象快速转换
     * @param pageData Spring Data 的 Page 对象
     * @return 分页响应对象
     */
    public static <T> MemberPageResponse<T> fromPage(org.springframework.data.domain.Page<T> pageData) {
        return MemberPageResponse.<T>builder()
                .records(pageData.getContent())
                .total(pageData.getTotalElements())
                .page(pageData.getNumber() + 1) // Spring Data 页码从 0 开始，转为 1
                .size(pageData.getSize())
                .pages(pageData.getTotalPages())
                .build();
    }
}