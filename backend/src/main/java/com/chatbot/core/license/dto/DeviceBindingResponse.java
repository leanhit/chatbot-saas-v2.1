package com.chatbot.core.license.dto;

import com.chatbot.core.license.model.DeviceBinding;
import com.chatbot.shared.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceBindingResponse {

    private Long id;
    private Long userId;
    private String deviceId;
    private String deviceName;
    private String status;
    
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    private Instant activatedAt;
    
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    private Instant lastSeenAt;

    public static DeviceBindingResponse from(DeviceBinding entity) {
        if (entity == null) {
            return null;
        }
        return DeviceBindingResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .deviceId(entity.getDeviceId())
                .deviceName(entity.getDeviceName())
                .status(entity.getStatus())
                .activatedAt(entity.getActivatedAt())
                .lastSeenAt(entity.getLastSeenAt())
                .build();
    }
}
