package com.gym.management.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CoachResponse {
    // 基本信息
    private Integer id;
    private String coachNo;           // 教练工号
    private String realName;          // 真实姓名
    private Integer gender;           // 性别：0 未知 1 男 2 女
    private String phone;             // 联系电话
    private String email;             // 邮箱
    
    // 职业信息
    private String coachType;         // 教练类型：store(门店专属)/free(自由教练)
    private Integer storeId;          // 所属门店 ID
    private String specialty;         // 专长
    private String level;             // 教练等级
    private BigDecimal hourlyRate;    // 课时费
    private String introduction;      // 个人介绍
    
    // 统计信息
    private Integer totalSessions;    // 累计上课次数
    private Integer status;           // 状态：1 在职 0 离职 2 休假
    
    // 扩展字段
    private String genderText;        // 性别文本
    private String statusText;        // 状态文本
    private String levelText;         // 等级文本
    private Boolean isAvailable;      // 是否可用
    private List<TimeSlotResponse> availableSlots;  // 空闲时段

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCoachNo() {
        return coachNo;
    }

    public void setCoachNo(String coachNo) {
        this.coachNo = coachNo;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCoachType() {
        return coachType;
    }

    public void setCoachType(String coachType) {
        this.coachType = coachType;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public Integer getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(Integer totalSessions) {
        this.totalSessions = totalSessions;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getGenderText() {
        return genderText;
    }

    public void setGenderText(String genderText) {
        this.genderText = genderText;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getLevelText() {
        return levelText;
    }

    public void setLevelText(String levelText) {
        this.levelText = levelText;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public List<TimeSlotResponse> getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(List<TimeSlotResponse> availableSlots) {
        this.availableSlots = availableSlots;
    }
}