// src/main/java/com/gym/management/dto/request/MemberQueryRequest.java

package com.gym.management.dto.request.admin;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 会员查询请求 DTO
 * 用于接收前端传来的筛选条件和分页参数
 * 对应数据库表：members
 */
@Data
public class MemberQueryRequest {

    // ================= 分页参数 =================

    /**
     * 页码 (默认第1页)
     * 前端通常从 1 开始，Service 层需转换为 0-based index
     */
    private Integer page = 1;

    /**
     * 每页数量 (默认10条)
     * 最大限制建议在 Service 层控制 (如 max=100)
     */
    private Integer size = 10;

    // ================= 通用搜索 =================

    /**
     * 模糊搜索关键词
     * 优先匹配：手机号(phone)、会员卡号(memberNo)、真实姓名(realName)
     * 如果以下具体字段有值，则 keyword 失效或作为补充条件
     */
    private String keyword;

    // ================= 精确筛选字段 (补充缺失项) =================

    /**
     * 真实姓名 (精确或模糊匹配)
     * 对应数据库 real_name
     */
    private String realName;

    /**
     * 手机号 (精确匹配或模糊匹配)
     * 对应数据库 phone
     */
    private String phone;

    /**
     * 会员卡号 (精确匹配)
     * 对应数据库 member_no
     */
    private String memberNo;

    /**
     * 性别筛选
     * 1: 男，2: 女，0: 未知
     * 对应数据库 gender
     */
    private Integer gender;

    /**
     * 会员状态筛选
     * 1: 正常 (ACTIVE)
     * 0: 过期 (EXPIRED)
     * 2: 冻结 (FROZEN)
     * 对应数据库 status
     */
    private Integer status;

    /**
     * 卡类型筛选
     * period: 期限卡
     * times: 次卡
     * vip: 贵宾卡
     * 对应数据库 card_type
     */
    private String cardType;

    /**
     * 注册门店 ID
     * 对应数据库 register_store_id
     */
    private Integer registerStoreId;

    // ================= 日期范围筛选 (新增重要功能) =================

    /**
     * 入会日期 - 开始时间 (包含)
     * 格式：yyyy-MM-dd
     * 对应数据库 join_date >= ?
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate joinDateStart;

    /**
     * 入会日期 - 结束时间 (包含)
     * 格式：yyyy-MM-dd
     * 对应数据库 join_date <= ?
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate joinDateEnd;

    /**
     * 到期日期 - 开始时间 (包含)
     * 用于筛选即将过期的会员
     * 对应数据库 expire_date >= ?
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expireDateStart;

    /**
     * 到期日期 - 结束时间 (包含)
     * 对应数据库 expire_date <= ?
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expireDateEnd;

    /**
     * 排序字段 (可选)
     * 默认: createTime
     * 可选: joinDate, expireDate, balance, visitCount
     */
    private String sortBy = "createTime";

    /**
     * 排序方向 (可选)
     * ASC: 升序，DESC: 降序
     * 默认: DESC
     */
    private String sortOrder = "DESC";
}