package com.chatbot.core.training.dto;

import com.chatbot.core.training.model.TrainingCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingCategoryResponse {
    private Long id;
    private String name;
    private String description;

    public static TrainingCategoryResponse from(TrainingCategory c) {
        if (c == null) return null;
        return TrainingCategoryResponse.builder()
                .id(c.getId()).name(c.getName()).description(c.getDescription()).build();
    }
}
