package com.gym.management.dto.request.admin;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
public class AnnouncementUpdateRequest {

    private Integer storeId;

    @Size(max = 200, message = "标题最多 200 字符")
    private String title;

    private String content;

    @Pattern(regexp = "^(all|single)$", message = "发布范围只能是 all 或 single")
    private String publishType;

    @Min(value = 1, message = "优先级最小为 1")
    @Max(value = 3, message = "优先级最大为 3")
    private Integer priority;

    @Min(value = 0, message = "状态最小为 0")
    @Max(value = 2, message = "状态最大为 2")
    private Integer publishStatus;

    private LocalDateTime publishTime;

    private LocalDateTime expireTime;
}