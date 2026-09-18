package com.chatbot.core.license.dto;

import com.chatbot.core.license.model.DeviceBinding;
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
    private Instant activatedAt;
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
