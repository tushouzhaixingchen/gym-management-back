// src/main/java/com/gym/management/dto/request/MemberVisitRequest.java
package com.gym.management.dto.request.admin;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

/**
 * 会员到店签到请求 DTO
 * 对应数据库表：member_visits
 */
@Data
public class MemberVisitRequest {

    /**
     * 会员 ID (必填)
     * 对应数据库: member_id
     */
    @NotNull(message = "会员ID不能为空")
    @Min(value = 1, message = "会员ID必须大于0")
    private Integer memberId;

    /**
     * 签到门店 ID (必填)
     * 对应数据库: store_id
     */
    @NotNull(message = "门店ID不能为空")
    @Min(value = 1, message = "门店ID必须大于0")
    private Integer storeId;

    /**
     * 签到方式 (可选，默认手动)
     * 对应数据库: check_in_method
     * 枚举值：manual(手动), card(刷卡), qrcode(二维码), face(人脸), device(设备)
     */
    private String checkInMethod;

    /**
     * 备注信息 / 设备编号 / 操作员备注 (可选)
     * 对应数据库: remark
     * 原 deviceInfo 合并至此
     */
    private String remark;

    /**
     * 关联预约 ID (可选，如果是私教课签到)
     * 对应数据库: related_appointment_id
     */
    private Integer relatedAppointmentId;

    /**
     * 关联课程 ID (可选，如果是团课签到)
     * 对应数据库: related_course_id
     */
    private Integer relatedCourseId;

    /**
     * 登记员工 ID (可选，如果是前台手动代签)
     * 对应数据库: staff_id
     * 通常由后端从登录上下文获取，若前端需透传可保留此字段
     */
    private Integer staffId;
}