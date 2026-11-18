package com.chatbot.core.training.dto;

import com.chatbot.core.training.model.CertificationLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationLevelResponse {
    private Long id;
    private String levelName;
    private String description;
    private String requiredTrainingIds;
    private String requiredSkillScores;
    private String requiredStats;
    private String badgeIcon;
    private String badgeColor;
    private Integer displayOrder;
    private Boolean active;
    private Integer validityDays;

    public static CertificationLevelResponse from(CertificationLevel l) {
        if (l == null) return null;
        return CertificationLevelResponse.builder()
                .id(l.getId()).levelName(l.getLevelName()).description(l.getDescription())
                .requiredTrainingIds(l.getRequiredTrainingIds())
                .requiredSkillScores(l.getRequiredSkillScores())
                .requiredStats(l.getRequiredStats())
                .badgeIcon(l.getBadgeIcon()).badgeColor(l.getBadgeColor())
                .displayOrder(l.getDisplayOrder()).active(l.getActive())
                .validityDays(l.getValidityDays())
                .build();
    }
}
