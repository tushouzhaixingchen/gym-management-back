package com.gym.management.dto.response;

import com.gym.management.entity.Store;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 门店信息响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponse {

    private Integer id;
    private String storeName;
    private String storeCode;
    private String province;
    private String city;
    private String district;
    private String address;
    private String phone;
    private String managerName;
    private String managerPhone;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer status;
    private String statusText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 从实体转换为响应对象
     */
    public static StoreResponse fromEntity(Store store) {
        if (store == null) {
            return null;
        }

        StoreResponse response = new StoreResponse();
        response.setId(store.getId());
        response.setStoreName(store.getStoreName());
        response.setStoreCode(store.getStoreCode());
        response.setProvince(store.getProvince());
        response.setCity(store.getCity());
        response.setDistrict(store.getDistrict());
        response.setAddress(store.getAddress());
        response.setPhone(store.getPhone());
        response.setManagerName(store.getManagerName());
        response.setManagerPhone(store.getManagerPhone());
        response.setOpenTime(store.getOpenTime());
        response.setCloseTime(store.getCloseTime());
        response.setStatus(store.getStatus());
        response.setStatusText(convertStatusToText(store.getStatus()));
        response.setCreatedAt(store.getCreatedAt());
        response.setUpdatedAt(store.getUpdatedAt());

        return response;
    }

    /**
     * 将状态码转换为文本描述
     */
    private static String convertStatusToText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "停业";
            case 1:
                return "营业中";
            case 2:
                return "装修中";
            default:
                return "未知";
        }
    }
}
