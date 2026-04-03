// src/main/java/com/gym/management/dto/request/MemberUpdateRequest.java

package com.gym.management.dto.request.admin;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 会员信息更新请求 DTO
 * 对应数据库表：members
 * 注意：所有字段均为可选（用于部分更新），但若提供则必须符合验证规则
 */
@Data
public class MemberUpdateRequest {

    /**
     * 真实姓名（可选）
     */
    @Size(max = 50, message = "姓名长度不能超过50个字符")
    private String realName;

    /**
     * 性别：0未知 1男 2女（可选）
     */
    @Min(value = 0, message = "性别值最小为0")
    @Max(value = 2, message = "性别值最大为2")
    private Integer gender;

    /**
     * 联系电话（可选，若修改需校验唯一性）
     */
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
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "出生日期格式不正确，应为 yyyy-MM-dd")
    private String birthday;

    /**
     * 会员到期日期（可选，格式：yyyy-MM-dd）
     * 通常由系统根据卡类型自动计算，但也允许手动调整
     */
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "到期日期格式不正确，应为 yyyy-MM-dd")
    private String expireDate;

    /**
     * 卡类型：period(期限卡)/times(次卡)/vip(贵宾卡)（可选）
     * ⚠️ 修改卡类型可能涉及复杂的业务逻辑（如退费、补差价），建议单独接口处理
     */
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
     * ⚠️ 直接修改余额存在风险，建议通过充值/消费接口处理
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
     * 次卡类型一致性验证
     * 如果当前是次卡，检查次数字段是否合理
     */
    public boolean validateTimesCardConsistency() {
        if ("times".equals(this.cardType)) {
            // 如果提供了次数字段，必须都大于等于0
            if (this.totalTimes != null && this.totalTimes < 0) return false;
            if (this.remainingTimes != null && this.remainingTimes < 0) return false;
            // 如果只改了一个，业务层需要判断逻辑是否合理（例如剩余不能大于总次数）
        }
        return true;
    }
}