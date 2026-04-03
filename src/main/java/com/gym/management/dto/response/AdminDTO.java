// dto/response/AdminDTO.java
package com.gym.management.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminDTO {

    private Integer id;
    private String username;
    private String realName;
    private Integer roleId;
    private String roleName;
    private String roleCode;
    private Integer storeId;
    private String storeName;
    private String phone;
    private String email;
    private Integer status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}