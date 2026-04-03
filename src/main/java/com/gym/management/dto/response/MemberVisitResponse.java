// src/main/java/com/gym/management/dto/response/MemberVisitResponse.java
package com.gym.management.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员到店签到响应 DTO
 * 用于返回签到结果及会员当前状态概览
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberVisitResponse {

    /**
     * 签到是否成功
     */
    private Boolean success;

    /**
     * 提示信息 (如: "签到成功", "余额不足", "非营业时间")
     */
    private String message;

    // --- 签到记录核心信息 ---

    /**
     * 签到记录 ID
     */
    private Integer id;

    /**
     * 会员 ID
     */
    private Integer memberId;

    /**
     * 会员姓名 (冗余字段，方便列表直接展示)
     */
    private String memberName;

    /**
     * 会员卡号
     */
    private String memberNo;

    /**
     * 门店 ID
     */
    private Integer storeId;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 签到时间
     */
    private LocalDateTime checkInTime;

    /**
     * 签到方式 (manual/card/qrcode/face/device)
     * 对应数据库: check_in_method
     */
    private String checkInMethod;

    /**
     * 本次使用的卡类型 (period/times/vip)
     * 对应数据库: card_type
     */
    private String cardType;

    /**
     * 本次扣除次数 (次卡专用，期限卡为0)
     * 对应数据库: deduct_times
     */
    private Integer deductTimes;

    // --- 会员当前状态概览 (用于前端展示) ---

    /**
     * 剩余有效期天数 (期限卡专用，次卡为null或0)
     */
    private Integer remainingDays;

    /**
     * 过期日期 (期限卡专用)
     * 建议使用 LocalDate 或 String，这里保持与原代码一致的 String 或改为 LocalDate
     */
    private String expiryDate;

    /**
     * 剩余次数 (次卡专用，期限卡为null)
     */
    private Integer remainingTimes;

    /**
     * 累计签到总次数 (统计字段)
     */
    private Integer totalCheckIns;

    /**
     * 会员详细对象 (可选，如果前端需要更多会员详情)
     * 如果不需要嵌套，可移除此字段以减少数据量
     */
    private MemberResponse memberInfo;
}