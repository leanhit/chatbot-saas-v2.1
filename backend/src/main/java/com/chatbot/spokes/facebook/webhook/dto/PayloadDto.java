package com.chatbot.spokes.facebook.webhook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayloadDto {
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
    private List<MessageDto.QuickReplyDto> quick_replies;
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
