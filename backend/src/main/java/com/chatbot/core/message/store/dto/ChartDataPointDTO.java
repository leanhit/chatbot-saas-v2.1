package com.chatbot.core.message.store.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChartDataPointDTO {
    
    private String label;
    private Long value;
    private String date; // ISO date string for time-based charts
    
    public ChartDataPointDTO() {}
    
    public ChartDataPointDTO(String label, Long value) {
        this.label = label;
        this.value = value;
    }
    
    public ChartDataPointDTO(String label, Long value, String date) {
        this.label = label;
        this.value = value;
        this.date = date;
    }
}
