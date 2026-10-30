package com.chatbot.core.customer.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CreateCustomerProfileRequest {
    private String externalId;
    private String name;
    private String email;
    private String phone;
    private String avatarUrl;
    private String status;
    private String tier;
    private String preferredLanguage;
    private String timezone;
    private Map<String, Object> customAttributes;
    private Map<String, Object> tags;
}
