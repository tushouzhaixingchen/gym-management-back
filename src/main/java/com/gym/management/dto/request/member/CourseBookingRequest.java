package com.gym.management.dto.request.member;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 会员课程报名请求
 */
@Data
public class CourseBookingRequest {

    @NotNull(message = "课程 ID 不能为空")
    private Integer courseId;

    @NotNull(message = "支付方式不能为空")
    private Integer payMethod; // 1微信 2支付宝 3现金

    private String remark; // 备注
}
