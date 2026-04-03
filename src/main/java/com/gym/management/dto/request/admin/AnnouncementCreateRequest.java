package com.gym.management.dto.request.admin;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
public class AnnouncementCreateRequest {

    private Integer storeId; // NULL 为全系统公告

    @NotBlank(message = "公告标题不能为空")
    @Size(max = 200, message = "标题最多 200 字符")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    private String content;

    @Pattern(regexp = "^(all|single)$", message = "发布范围只能是 all 或 single")
    private String publishType = "all";

    @Min(value = 1, message = "优先级最小为 1")
    @Max(value = 3, message = "优先级最大为 3")
    private Integer priority = 1;

    @Min(value = 0, message = "状态最小为 0")
    @Max(value = 1, message = "状态最大为 1")
    private Integer publishStatus = 0; // 0 草稿 1 已发布

    private LocalDateTime publishTime; // 不传则发布时自动设置

    private LocalDateTime expireTime;
}