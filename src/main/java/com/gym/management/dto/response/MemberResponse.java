// src/main/java/com/gym/management/dto/response/MemberResponse.java

package com.gym.management.dto.response;

import com.gym.management.entity.Member;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 会员信息响应 DTO (视图对象)
 * 负责将数据库实体转换为前端友好的格式
 * - 状态码 -> 中文描述
 * - 性别码 -> 中文描述
 * - 卡类型码 -> 中文描述
 * - 自动计算剩余天数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {

    // ================= 基础信息 =================

    private Integer id;

    /** 会员编号 (如：M20260320001) */
    private String memberNo;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 性别描述 (男/女/未知) - 前端直接展示用 */
    private String genderText;

    /** 性别原始值 (0/1/2) - 方便前端做逻辑判断 */
    private Integer gender;

    /** 出生日期 */
    private LocalDate birthday;

    /** 邮箱 */
    private String email;

    // ================= 门店信息 =================

    /** 注册门店 ID */
    private Integer registerStoreId;

    /** 注册门店名称 (关联查询填充) */
    private String registerStoreName;

    // ================= 卡片与状态 =================

    /** 卡类型描述 (期限卡/次卡/贵宾卡) */
    private String cardTypeText;

    /** 卡类型原始值 (period/times/vip) */
    private String cardType;

    /**
     * 总次数 (仅次卡有效)
     */
    private Integer totalTimes;

    /**
     * 剩余次数 (仅次卡有效)
     */
    private Integer remainingTimes;

    /** 账户余额 */
    private BigDecimal balance;

    /** 状态描述 (正常/过期/冻结) */
    private String statusText;

    /** 状态原始值 (1/0/2) */
    private Integer status;

    /** 入会日期 */
    private LocalDate joinDate;

    /** 到期日期 */
    private LocalDate expireDate;

    /**
     * 剩余天数 (动态计算)
     * 逻辑：如果状态是"正常"，计算 expireDate - today；否则为 0 或负数
     */
    private Integer remainingDays;

    // ================= 统计信息 =================

    /** 累计消费金额 */
    private BigDecimal totalConsumption;

    /** 累计到店次数 */
    private Integer visitCount;

    /** 最后签到时间 */
    private LocalDateTime lastCheckInTime;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    // ================= 🔥 新增字段 =================

    /**
     * 临时密码 (明文)
     * 仅在 [新增会员] 或 [重置密码] 接口返回，其他查询接口为 null
     * ⚠️ 注意：此密码仅展示一次，请勿记录日志或存入数据库
     */
    private String temporaryPassword;

    // ================= 静态转换方法 (核心逻辑) =================

    /**
     * 将数据库 Entity 转换为 Response DTO
     * @param entity 数据库实体对象 (假设名为 Member)
     * @param storeName 门店名称 (可能来自关联查询)
     * @return 响应对象
     */
    public static MemberResponse fromEntity(Object entity, String storeName) {
        if (!(entity instanceof Member)) {
            throw new IllegalArgumentException("Entity must be of type Member");
        }

        Member m = (Member) entity;
        return MemberResponse.builder()
                .id(m.getId())
                .memberNo(m.getMemberNo())
                .realName(m.getRealName())
                .phone(m.getPhone())
                .gender(m.getGender())
                .genderText(getGenderText(m.getGender()))
                .birthday(m.getBirthday())
                .email(m.getEmail())
                .registerStoreId(m.getRegisterStoreId())
                .registerStoreName(storeName)
                .cardType(m.getCardType())
                .cardTypeText(getCardTypeText(m.getCardType()))
                .totalTimes(m.getTotalTimes())
                .remainingTimes(m.getRemainingTimes())
                .balance(m.getBalance())
                .status(m.getStatus())
                .statusText(getStatusText(m.getStatus()))
                .joinDate(m.getJoinDate())
                .expireDate(m.getExpireDate())
                .remainingDays(calculateRemainingDays(m.getStatus(), m.getExpireDate()))
                .totalConsumption(m.getTotalConsumption())
                .visitCount(m.getVisitCount())
                .lastCheckInTime(m.getLastVisitAt())
                .remark(m.getRemark())
                .createTime(m.getCreatedAt())
                .updateTime(m.getUpdatedAt())
                // 🔥 默认设为 null，只有在特定业务场景下由 Service 手动设置
                .temporaryPassword(null)
                .build();
    }

    // ================= 辅助转换逻辑 =================

    /**
     * 性别码转文本
     */
    public static String getGenderText(Integer gender) {
        if (gender == null) return "未知";
        switch (gender) {
            case 1: return "男";
            case 2: return "女";
            default: return "未知";
        }
    }

    /**
     * 状态码转文本
     * 1=正常，0=过期，2=冻结
     */
    public static String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "正常";
            case 0: return "过期";
            case 2: return "冻结";
            default: return "未知";
        }
    }

    /**
     * 卡类型转文本
     * period=期限卡，times=次卡，vip=贵宾卡
     */
    public static String getCardTypeText(String cardType) {
        if (cardType == null) return "未知";
        switch (cardType) {
            case "period": return "期限卡";
            case "times": return "次卡";
            case "vip": return "贵宾卡";
            default: return "未知";
        }
    }

    /**
     * 计算剩余天数
     * 只有状态为"正常"(1) 且 有到期日期时才计算
     */
    public static Integer calculateRemainingDays(Integer status, LocalDate expireDate) {
        if (status == null || !status.equals(1) || expireDate == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), expireDate);
        return days < 0 ? 0 : (int) days;
    }
}