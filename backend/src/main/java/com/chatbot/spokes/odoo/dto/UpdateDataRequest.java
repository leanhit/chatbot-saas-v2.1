package com.chatbot.spokes.odoo.dto;

import com.chatbot.spokes.odoo.model.CustomerStatus;
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