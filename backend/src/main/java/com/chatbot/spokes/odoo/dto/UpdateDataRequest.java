package com.chatbot.integrations.odoo.dto;

import com.chatbot.integrations.odoo.model.CustomerStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDataRequest {
    @JsonProperty("dataJson")
    private String dataJson;

    @JsonProperty("status")
    private CustomerStatus status;
}