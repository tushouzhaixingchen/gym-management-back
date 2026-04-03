// dto/response/AdminDetailVO.java
package com.gym.management.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminDetailVO extends AdminDTO {

    private String lastLoginIp;
    private LocalDateTime updatedAt;
}