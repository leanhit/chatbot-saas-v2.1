package com.chatbot.spokes.facebook.webhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEventDto {
    
    @JsonProperty("object")
    private String object;
    
    private List<EntryDto> entry;
    
    // Forward-declared inner classes to resolve forward references
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickReplyDto {
        private String title;
        private String payload;
        private String content_type;
        private Map<String, Object> image_url;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntityDto {
        private String type;
        private String text;
        private Double offset;
        private Double length;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntryDto {
        private String id;
        private Long time;
        private List<MessagingDto> messaging;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MessagingDto {
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
            public static class MessageDto {
                private String mid;
                private String text;
                private List<AttachmentDto> attachments;
                private QuickReplyDto quick_reply;
                private List<EntityDto> entities;
                private Boolean is_echo;
                private String app_id;
                private Map<String, Object> metadata;
                
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class AttachmentDto {
                    private String type;
                    private PayloadDto payload;
                    
                    @Data
                    @Builder
                    @NoArgsConstructor
                    @AllArgsConstructor
                    public static class PayloadDto {
                        private String url;
                        private String sticker_id;
                        private List<ButtonDto> buttons;
                        private List<ElementDto> elements;
                        private String template_type;
                        private String text;
                        private String subtitle;
                        private String title;
                        private String image_url;
                        private String fallback_url;
                        private String webview_height_ratio;
                        private String webview_share_button;
                        private String messenger_extensions;
                        private String shareable;
                        private String coordinates;
                        private List<QuickReplyDto> quick_replies;
                        private String aspect_ratio;
                        private String media_type;
                        private String is_sharable;
                        private String attachment_id;
                        private List<ProductDto> products;
                        private String merchant_settings;
                        private String payment_type;
                        private String payment_summary;
                        private String amount;
                        private String currency;
                        private String payment_method;
                        private String credential;
                        private String shipping_option;
                        private String is_test;
                        private String audience_type;
                        private String broadcaster;
                        private String created_time;
                        private String updated_time;
                        private String is_deleted;
                        private String message_tag;
                        private String notification_messages;
                        private String seq;
                        private String watermark;
                        private String label;
                        private String color;
                        private List<CoordinateDto> coordinates_list;
                        private String limit;
                        private String after;
                        private String before;
                        private String since;
                        private String until;
                        private String object;
                        private String entry;
                        private String field;
                        private String value;
                        private String changed_fields;
                        private String real_time;
                        private String standby;
                        private String live_person;
                        private String live_person_id;
                        private String live_person_name;
                        private String live_person_profile_pic;
                        private String is_subscribed;
                        private String subscription_id;
                        private String product_id;
                        private String product_title;
                        private String product_description;
                        private String product_price;
                        private String product_image_url;
                        private String product_url;
                        private String product_retailer_id;
                        private String product_quantity;
                        private String product_merchant_id;
                        private String product_condition;
                        private String product_availability;
                        private String product_brand;
                        private String product_category;
                        private String product_google_product_category;
                        private String product_gtin;
                        private String product_mpn;
                        private String product_size;
                        private String product_color;
                        private String product_gender;
                        private String product_age_group;
                        private String product_material;
                        private String product_pattern;
                        private String product_shipping_weight;
                        private String product_shipping_weight_unit;
                        private String product_group_id;
                        private String product_item_group_id;
                        private String product_adult;
                        private String product_custom_label_0;
                        private String product_custom_label_1;
                        private String product_custom_label_2;
                        private String product_custom_label_3;
                        private String product_custom_label_4;
                        private String product_custom_label_5;
                        private String product_custom_label_6;
                        private String product_custom_label_7;
                        private String product_custom_label_8;
                        private String product_custom_label_9;
                        private String product_custom_label_10;
                        private String product_custom_label_11;
                        private String product_custom_label_12;
                        private String product_custom_label_13;
                        private String product_custom_label_14;
                        private String product_custom_label_15;
                        private String product_custom_label_16;
                        private String product_custom_label_17;
                        private String product_custom_label_18;
                        private String product_custom_label_19;
                        private String product_custom_label_20;
                        private String product_custom_label_21;
                        private String product_custom_label_22;
                        private String product_custom_label_23;
                        private String product_custom_label_24;
                        private String product_custom_label_25;
                        private String product_custom_label_26;
                        private String product_custom_label_27;
                        private String product_custom_label_28;
                        private String product_custom_label_29;
                        private String product_custom_label_30;
                        private String product_custom_label_31;
                        private String product_custom_label_32;
                        private String product_custom_label_33;
                        private String product_custom_label_34;
                        private String product_custom_label_35;
                        private String product_custom_label_36;
                        private String product_custom_label_37;
                        private String product_custom_label_38;
                        private String product_custom_label_39;
                        private String product_custom_label_40;
                        private String product_custom_label_41;
                        private String product_custom_label_42;
                        private String product_custom_label_43;
                        private String product_custom_label_44;
                        private String product_custom_label_45;
                        private String product_custom_label_46;
                        private String product_custom_label_47;
                        private String product_custom_label_48;
                        private String product_custom_label_49;
                        private String product_custom_label_50;
                        private String product_custom_label_51;
                        private String product_custom_label_52;
                        private String product_custom_label_53;
                        private String product_custom_label_54;
                        private String product_custom_label_55;
                        private String product_custom_label_56;
                        private String product_custom_label_57;
                        private String product_custom_label_58;
                        private String product_custom_label_59;
                        private String product_custom_label_60;
                        private String product_custom_label_61;
                        private String product_custom_label_62;
                        private String product_custom_label_63;
                        private String product_custom_label_64;
                        private String product_custom_label_65;
                        private String product_custom_label_66;
                        private String product_custom_label_67;
                        private String product_custom_label_68;
                        private String product_custom_label_69;
                        private String product_custom_label_70;
                        private String product_custom_label_71;
                        private String product_custom_label_72;
                        private String product_custom_label_73;
                        private String product_custom_label_74;
                        private String product_custom_label_75;
                        private String product_custom_label_76;
                        private String product_custom_label_77;
                        private String product_custom_label_78;
                        private String product_custom_label_79;
                        private String product_custom_label_80;
                        private String product_custom_label_81;
                        private String product_custom_label_82;
                        private String product_custom_label_83;
                        private String product_custom_label_84;
                        private String product_custom_label_85;
                        private String product_custom_label_86;
                        private String product_custom_label_87;
                        private String product_custom_label_88;
                        private String product_custom_label_89;
                        private String product_custom_label_90;
                        private String product_custom_label_91;
                        private String product_custom_label_92;
                        private String product_custom_label_93;
                        private String product_custom_label_94;
                        private String product_custom_label_95;
                        private String product_custom_label_96;
                        private String product_custom_label_97;
                        private String product_custom_label_98;
                        private String product_custom_label_99;
                        private String product_custom_label_100;
                    }
                    
                    @Data
                    @Builder
                    @NoArgsConstructor
                    @AllArgsConstructor
                    public static class ButtonDto {
                        private String type;
                        private String url;
                        private String title;
                        private String webview_height_ratio;
                        private String messenger_extensions;
                        private String fallback_url;
                        private String webview_share_button;
                        private String phone_number;
                        private String payload;
                    }
                    
                    @Data
                    @Builder
                    @NoArgsConstructor
                    @AllArgsConstructor
                    public static class ElementDto {
                        private String title;
                        private String subtitle;
                        private String image_url;
                        private List<ButtonDto> buttons;
                        private String item_url;
                        private String default_action;
                        private Map<String, Object> quantity;
                        private Map<String, Object> price;
                        private Map<String, Object> currency;
                        private Map<String, Object> subtitle_text;
                        private Map<String, Object> title_text;
                        private Map<String, Object> image_url_text;
                        private Map<String, Object> buttons_text;
                        private Map<String, Object> item_url_text;
                        private Map<String, Object> default_action_text;
                        private Map<String, Object> quantity_text;
                        private Map<String, Object> price_text;
                        private Map<String, Object> currency_text;
                    }
                    
                                        
                    @Data
                    @Builder
                    @NoArgsConstructor
                    @AllArgsConstructor
                    public static class CoordinateDto {
                        private Double lat;
                        private Double lon;
                    }
                    
                    @Data
                    @Builder
                    @NoArgsConstructor
                    @AllArgsConstructor
                    public static class ProductDto {
                        private String merchant_id;
                        private String product_id;
                        private String product_retailer_id;
                        private String title;
                        private String description;
                        private String price;
                        private String currency;
                        private String image_url;
                        private String url;
                        private String condition;
                        private String availability;
                        private String brand;
                        private String category;
                        private String google_product_category;
                        private String gtin;
                        private String mpn;
                        private String size;
                        private String color;
                        private String gender;
                        private String age_group;
                        private String material;
                        private String pattern;
                        private String shipping_weight;
                        private String shipping_weight_unit;
                        private String group_id;
                        private String item_group_id;
                        private String adult;
                        private List<String> custom_label;
                    }
                }
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
                private List<String> mids;
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
            public static class OptinDto {
                private String ref;
                private String user_ref;
                private List<String> user_ref_source;
                private String type;
                private String ad_id;
                private String publisher_id;
                private String campaign_id;
                private String adgroup_id;
                private String leadgen_did;
                private String leadgen_form_id;
                private String page_id;
                private String form_id;
                private String is_user_ref;
                private String is_bot_ref;
                private String is_direct_ref;
                private String is_coupon_ref;
                private String is_from_ads;
                private String is_from_messenger_ads;
                private String is_from_messenger_ads_2;
                private String is_from_messenger_ads_3;
                private String is_from_messenger_ads_4;
                private String is_from_messenger_ads_5;
                private String is_from_messenger_ads_6;
                private String is_from_messenger_ads_7;
                private String is_from_messenger_ads_8;
                private String is_from_messenger_ads_9;
                private String is_from_messenger_ads_10;
                private String is_from_messenger_ads_11;
                private String is_from_messenger_ads_12;
                private String is_from_messenger_ads_13;
                private String is_from_messenger_ads_14;
                private String is_from_messenger_ads_15;
                private String is_from_messenger_ads_16;
                private String is_from_messenger_ads_17;
                private String is_from_messenger_ads_18;
                private String is_from_messenger_ads_19;
                private String is_from_messenger_ads_20;
                private String is_from_messenger_ads_21;
                private String is_from_messenger_ads_22;
                private String is_from_messenger_ads_23;
                private String is_from_messenger_ads_24;
                private String is_from_messenger_ads_25;
                private String is_from_messenger_ads_26;
                private String is_from_messenger_ads_27;
                private String is_from_messenger_ads_28;
                private String is_from_messenger_ads_29;
                private String is_from_messenger_ads_30;
                private String is_from_messenger_ads_31;
                private String is_from_messenger_ads_32;
                private String is_from_messenger_ads_33;
                private String is_from_messenger_ads_34;
                private String is_from_messenger_ads_35;
                private String is_from_messenger_ads_36;
                private String is_from_messenger_ads_37;
                private String is_from_messenger_ads_38;
                private String is_from_messenger_ads_39;
                private String is_from_messenger_ads_40;
                private String is_from_messenger_ads_41;
                private String is_from_messenger_ads_42;
                private String is_from_messenger_ads_43;
                private String is_from_messenger_ads_44;
                private String is_from_messenger_ads_45;
                private String is_from_messenger_ads_46;
                private String is_from_messenger_ads_47;
                private String is_from_messenger_ads_48;
                private String is_from_messenger_ads_49;
                private String is_from_messenger_ads_50;
                private String is_from_messenger_ads_51;
                private String is_from_messenger_ads_52;
                private String is_from_messenger_ads_53;
                private String is_from_messenger_ads_54;
                private String is_from_messenger_ads_55;
                private String is_from_messenger_ads_56;
                private String is_from_messenger_ads_57;
                private String is_from_messenger_ads_58;
                private String is_from_messenger_ads_59;
                private String is_from_messenger_ads_60;
                private String is_from_messenger_ads_61;
                private String is_from_messenger_ads_62;
                private String is_from_messenger_ads_63;
                private String is_from_messenger_ads_64;
                private String is_from_messenger_ads_65;
                private String is_from_messenger_ads_66;
                private String is_from_messenger_ads_67;
                private String is_from_messenger_ads_68;
                private String is_from_messenger_ads_69;
                private String is_from_messenger_ads_70;
                private String is_from_messenger_ads_71;
                private String is_from_messenger_ads_72;
                private String is_from_messenger_ads_73;
                private String is_from_messenger_ads_74;
                private String is_from_messenger_ads_75;
                private String is_from_messenger_ads_76;
                private String is_from_messenger_ads_77;
                private String is_from_messenger_ads_78;
                private String is_from_messenger_ads_79;
                private String is_from_messenger_ads_80;
                private String is_from_messenger_ads_81;
                private String is_from_messenger_ads_82;
                private String is_from_messenger_ads_83;
                private String is_from_messenger_ads_84;
                private String is_from_messenger_ads_85;
                private String is_from_messenger_ads_86;
                private String is_from_messenger_ads_87;
                private String is_from_messenger_ads_88;
                private String is_from_messenger_ads_89;
                private String is_from_messenger_ads_90;
                private String is_from_messenger_ads_91;
                private String is_from_messenger_ads_92;
                private String is_from_messenger_ads_93;
                private String is_from_messenger_ads_94;
                private String is_from_messenger_ads_95;
                private String is_from_messenger_ads_96;
                private String is_from_messenger_ads_97;
                private String is_from_messenger_ads_98;
                private String is_from_messenger_ads_99;
                private String is_from_messenger_ads_100;
            }
            
            @Data
            @Builder
            @NoArgsConstructor
            @AllArgsConstructor
            public static class ReferralDto {
                private String ref;
                private String source;
                private String type;
                private String ad_id;
                private String publisher_id;
                private String campaign_id;
                private String adgroup_id;
                private String leadgen_did;
                private String leadgen_form_id;
                private String page_id;
                private String form_id;
                private String is_user_ref;
                private String is_bot_ref;
                private String is_direct_ref;
                private String is_coupon_ref;
                private String is_from_ads;
                private String is_from_messenger_ads;
                private String is_from_messenger_ads_2;
                private String is_from_messenger_ads_3;
                private String is_from_messenger_ads_4;
                private String is_from_messenger_ads_5;
                private String is_from_messenger_ads_6;
                private String is_from_messenger_ads_7;
                private String is_from_messenger_ads_8;
                private String is_from_messenger_ads_9;
                private String is_from_messenger_ads_10;
                private String is_from_messenger_ads_11;
                private String is_from_messenger_ads_12;
                private String is_from_messenger_ads_13;
                private String is_from_messenger_ads_14;
                private String is_from_messenger_ads_15;
                private String is_from_messenger_ads_16;
                private String is_from_messenger_ads_17;
                private String is_from_messenger_ads_18;
                private String is_from_messenger_ads_19;
                private String is_from_messenger_ads_20;
                private String is_from_messenger_ads_21;
                private String is_from_messenger_ads_22;
                private String is_from_messenger_ads_23;
                private String is_from_messenger_ads_24;
                private String is_from_messenger_ads_25;
                private String is_from_messenger_ads_26;
                private String is_from_messenger_ads_27;
                private String is_from_messenger_ads_28;
                private String is_from_messenger_ads_29;
                private String is_from_messenger_ads_30;
                private String is_from_messenger_ads_31;
                private String is_from_messenger_ads_32;
                private String is_from_messenger_ads_33;
                private String is_from_messenger_ads_34;
                private String is_from_messenger_ads_35;
                private String is_from_messenger_ads_36;
                private String is_from_messenger_ads_37;
                private String is_from_messenger_ads_38;
                private String is_from_messenger_ads_39;
                private String is_from_messenger_ads_40;
                private String is_from_messenger_ads_41;
                private String is_from_messenger_ads_42;
                private String is_from_messenger_ads_43;
                private String is_from_messenger_ads_44;
                private String is_from_messenger_ads_45;
                private String is_from_messenger_ads_46;
                private String is_from_messenger_ads_47;
                private String is_from_messenger_ads_48;
                private String is_from_messenger_ads_49;
                private String is_from_messenger_ads_50;
                private String is_from_messenger_ads_51;
                private String is_from_messenger_ads_52;
                private String is_from_messenger_ads_53;
                private String is_from_messenger_ads_54;
                private String is_from_messenger_ads_55;
                private String is_from_messenger_ads_56;
                private String is_from_messenger_ads_57;
                private String is_from_messenger_ads_58;
                private String is_from_messenger_ads_59;
                private String is_from_messenger_ads_60;
                private String is_from_messenger_ads_61;
                private String is_from_messenger_ads_62;
                private String is_from_messenger_ads_63;
                private String is_from_messenger_ads_64;
                private String is_from_messenger_ads_65;
                private String is_from_messenger_ads_66;
                private String is_from_messenger_ads_67;
                private String is_from_messenger_ads_68;
                private String is_from_messenger_ads_69;
                private String is_from_messenger_ads_70;
                private String is_from_messenger_ads_71;
                private String is_from_messenger_ads_72;
                private String is_from_messenger_ads_73;
                private String is_from_messenger_ads_74;
                private String is_from_messenger_ads_75;
                private String is_from_messenger_ads_76;
                private String is_from_messenger_ads_77;
                private String is_from_messenger_ads_78;
                private String is_from_messenger_ads_79;
                private String is_from_messenger_ads_80;
                private String is_from_messenger_ads_81;
                private String is_from_messenger_ads_82;
                private String is_from_messenger_ads_83;
                private String is_from_messenger_ads_84;
                private String is_from_messenger_ads_85;
                private String is_from_messenger_ads_86;
                private String is_from_messenger_ads_87;
                private String is_from_messenger_ads_88;
                private String is_from_messenger_ads_89;
                private String is_from_messenger_ads_90;
                private String is_from_messenger_ads_91;
                private String is_from_messenger_ads_92;
                private String is_from_messenger_ads_93;
                private String is_from_messenger_ads_94;
                private String is_from_messenger_ads_95;
                private String is_from_messenger_ads_96;
                private String is_from_messenger_ads_97;
                private String is_from_messenger_ads_98;
                private String is_from_messenger_ads_99;
                private String is_from_messenger_ads_100;
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
                private List<String> app_roles;
            }
            
            @Data
            @Builder
            @NoArgsConstructor
            @AllArgsConstructor
            public static class StandbyDto {
                private String app_id;
            }
        }
    }
}
