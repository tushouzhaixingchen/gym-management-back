// src/main/java/com/gym/management/dto/request/MemberStatusRequest.java
package com.gym.management.dto.request.admin;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class MemberStatusRequest {

    /**
     * 目标状态: 1=正常，0=过期，2=冻结
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 变更原因 (特别是冻结时必填)
     */
    private String reason;
}