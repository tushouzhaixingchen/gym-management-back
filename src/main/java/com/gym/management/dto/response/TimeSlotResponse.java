package com.gym.management.dto.response;

import lombok.Getter;

// 时间段响应（通用）
@Getter
public class TimeSlotResponse {
    private String start; // "09:00"
    private String end;   // "10:00"

    // 构造方法、Getters and Setters
    public TimeSlotResponse(String start, String end) {
        this.start = start;
        this.end = end;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}