package com.chatbot.spokes.facebook.webhook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessagingDto {
    private SenderDto sender;
    private RecipientDto recipient;
    private Long timestamp;
    private MessageDto message;
    private PostbackDto postback;
    private DeliveryDto delivery;
    private ReadDto read;
    private OptinDto optin;
    private ReferralDto referral;
    private GamePlayDto game_play;
    private PolicyEnforcementDto policy_enforcement;
    private PassThreadControlDto pass_thread_control;
    private TakeThreadControlDto take_thread_control;
    private AppRolesDto app_roles;
    private StandbyDto standby;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SenderDto {
        private String id;
        private String user_ref;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipientDto {
        private String id;
        private String user_ref;
        private String phone_number;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostbackDto {
        private String payload;
        private String title;
        private ReferralDto referral;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryDto {
        private java.util.List<String> mids;
        private Long watermark;
        private Long seq;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReadDto {
        private Long watermark;
        private Long seq;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GamePlayDto {
        private String player_id;
        private String context_type;
        private String score;
        private String game_play;
        private String payload;
        private String title;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PolicyEnforcementDto {
        private String action;
        private String reason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PassThreadControlDto {
        private String new_owner_app_id;
        private String metadata;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TakeThreadControlDto {
        private String previous_owner_app_id;
        private String metadata;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppRolesDto {
        private java.util.List<String> app_roles;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StandbyDto {
        private String app_id;
    }
}
