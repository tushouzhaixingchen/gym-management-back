// src/main/java/com/gym/management/dto/request/MemberCreateRequest.java

package com.gym.management.dto.request.admin;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 会员创建请求 DTO
 * 对应数据库表：members
 */
@Data
public class MemberCreateRequest {

    /**
     * 真实姓名（必填）
     */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50个字符")
    private String realName;

    /**
     * 性别：0未知 1男 2女（必填）
     */
    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别值最小为0")
    @Max(value = 2, message = "性别值最大为2")
    private Integer gender;

    /**
     * 联系电话（必填）
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 邮箱（可选）
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    /**
     * 出生日期（可选，格式：yyyy-MM-dd）
     */
    private LocalDate birthday;

    /**
     * 注册门店ID（必填，首次办卡门店）
     */
    @NotNull(message = "注册门店ID不能为空")
    private Integer registerStoreId;

    /**
     * 入会日期（必填，格式：yyyy-MM-dd）
     */
    @NotBlank(message = "入会日期不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "入会日期格式不正确，应为 yyyy-MM-dd")
    private String joinDate;

    /**
     * 会员到期日期（可选，格式：yyyy-MM-dd）
     */
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "到期日期格式不正确，应为 yyyy-MM-dd")
    private String expireDate;

    /**
     * 卡类型：period(期限卡)/times(次卡)/vip(贵宾卡)（必填）
     */
    @NotBlank(message = "卡类型不能为空")
    @Pattern(regexp = "^(period|times|vip)$", message = "卡类型只能是 period/times/vip")
    private String cardType;

    /**
     * 总次数（次卡专用，可选）
     */
    @Min(value = 0, message = "总次数不能为负数")
    private Integer totalTimes;

    /**
     * 剩余次数（次卡专用，可选）
     */
    @Min(value = 0, message = "剩余次数不能为负数")
    private Integer remainingTimes;

    /**
     * 账户余额（可选，默认0）
     */
    @DecimalMin(value = "0.00", message = "余额不能为负数")
    @Digits(integer = 8, fraction = 2, message = "余额格式不正确")
    private BigDecimal balance;

    /**
     * 备注（可选）
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    // ================= 辅助验证方法 =================

    /**
     * 次卡类型验证：如果是次卡，必须设置总次数和剩余次数
     */
    public boolean validateTimesCard() {
        if ("times".equals(this.cardType)) {
            return this.totalTimes != null && this.totalTimes > 0
                    && this.remainingTimes != null && this.remainingTimes >= 0;
        }
        return true;
    }

    /**
     * 期限卡验证：如果是期限卡，必须设置到期日期
     */
    public boolean validatePeriodCard() {
        if ("period".equals(this.cardType)) {
            return this.expireDate != null && !this.expireDate.isEmpty();
        }
        return true;
    }
}