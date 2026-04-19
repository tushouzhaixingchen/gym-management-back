package com.gym.management.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员个人信息响应VO
 */
@Data
public class MemberProfileVO {

    /** 会员ID */
    private Integer id;

    /** 会员卡号 */
    private String memberNo;

    /** 真实姓名 */
    private String realName;

    /** 性别：0未知 1男 2女 */
    private Integer gender;

    /** 性别文本 */
    private String genderText;

    /** 联系电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 出生日期 */
    private LocalDate birthday;

    /** 注册门店ID */
    private Integer registerStoreId;

    /** 入会日期 */
    private LocalDate joinDate;

    /** 会员到期日期 */
    private LocalDate expireDate;

    /** 卡类型：period(期限卡)/times(次卡)/vip(贵宾卡) */
    private String cardType;

    /** 总次数（次卡用） */
    private Integer totalTimes;

    /** 剩余次数（次卡用） */
    private Integer remainingTimes;

    /** 账户余额 */
    private BigDecimal balance;

    /** 累计消费金额 */
    private BigDecimal totalConsumption;

    /** 总到店次数 */
    private Integer visitCount;

    /** 最后到店时间 */
    private LocalDateTime lastVisitAt;

    /** 最后到店门店ID */
    private Integer lastVisitStoreId;

    /** 状态：1正常 0过期 2冻结 */
    private Integer status;

    /** 状态文本 */
    private String statusText;

    /** 是否过期 */
    private Boolean expired;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
