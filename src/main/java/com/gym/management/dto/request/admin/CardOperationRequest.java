// src/main/java/com/gym/management/dto/request/CardOperationRequest.java
package com.gym.management.dto.request.admin;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class CardOperationRequest {

    /**
     * 卡类型: period(期限卡), times(次卡), vip(贵宾卡)
     */
    @NotBlank(message = "卡类型不能为空")
    private String cardType;

    /**
     * 期限卡有效期（月数），仅当 cardType=period 时有效
     */
    @Positive(message = "有效期月数必须大于0")
    private Integer durationMonths;

    /**
     * 次卡总次数，仅当 cardType=times 时有效
     */
    @Positive(message = "次数必须大于0")
    private Integer timesCount;

    /**
     * 支付金额
     */
    @NotNull(message = "金额不能为空")
    @Positive(message = "金额必须大于0")
    private BigDecimal price;

    /**
     * 备注
     */
    private String remark;
}