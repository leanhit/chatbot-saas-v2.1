package com.chatbot.spokes.facebook.webhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSendRequestDto {
    
    @NotBlank(message = "Recipient is required")
    private RecipientDto recipient;
    
    @NotNull(message = "Message is required")
    private MessageDto message;
    
    private String tag;
    private String notificationType;
    private String personaId;
    private String customThreadId;
    private String senderActionId;
    private Map<String, Object> customPayload;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipientDto {
        private String id;
        
        @JsonProperty("user_ref")
        private String userRef;
        
        private String phoneNumber;
        
        @JsonProperty("phone_number")
        private String phoneNumberFull;
        
        private NameDto name;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class NameDto {
            private String firstName;
            private String lastName;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageDto {
        private String text;
        private String attachmentId;
        private List<AttachmentDto> attachments;
        private QuickReplyDto quickReply;
        
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
                private String stickerId;
                private List<ButtonDto> buttons;
                private List<ElementDto> elements;
                private String templateType;
                private String text;
                private String subtitle;
                private String title;
                private String imageUrl;
                private String fallbackUrl;
                private String webviewHeightRatio;
                private String webviewShareButton;
                private String messengerExtensions;
                private String shareable;
                private String coordinates;
                private List<QuickReplyDto> quickReplies;
                private String aspectRatio;
                private String mediaType;
                private String isSharable;
                private String attachmentId;
                private List<ProductDto> products;
                private String merchantSettings;
                private String paymentType;
                private String paymentSummary;
                private String amount;
                private String currency;
                private String paymentMethod;
                private String credential;
                private String shippingOption;
                private String isTest;
                private String audienceType;
                private String broadcaster;
                private String createdTime;
                private String updatedTime;
                private String isDeleted;
                private String messageTag;
                private String notificationMessages;
                private String seq;
                private String watermark;
                private String label;
                private String color;
                private List<CoordinateDto> coordinatesList;
                private String limit;
                private String after;
                private String before;
                private String since;
                private String until;
                private String object;
                private String entry;
                private String field;
                private String value;
                private String changedFields;
                private String realTime;
                private String standby;
                private String livePerson;
                private String livePersonId;
                private String livePersonName;
                private String livePersonProfilePic;
                private String isSubscribed;
                private String subscriptionId;
                private String productId;
                private String productTitle;
                private String productDescription;
                private String productPrice;
                private String productImageUrl;
                private String productUrl;
                private String productRetailerId;
                private String productQuantity;
                private String productMerchantId;
                private String productCondition;
                private String productAvailability;
                private String productBrand;
                private String productCategory;
                private String productGoogleProductCategory;
                private String productGtin;
                private String productMpn;
                private String productSize;
                private String productColor;
                private String productGender;
                private String productAgeGroup;
                private String productMaterial;
                private String productPattern;
                private String productShippingWeight;
                private String productShippingWeightUnit;
                private String productGroupId;
                private String productItemGroupId;
                private String productAdult;
                private String productCustomLabel0;
                private String productCustomLabel1;
                private String productCustomLabel2;
                private String productCustomLabel3;
                private String productCustomLabel4;
                private String productCustomLabel5;
                private String productCustomLabel6;
                private String productCustomLabel7;
                private String productCustomLabel8;
                private String productCustomLabel9;
                private String productCustomLabel10;
                private String productCustomLabel11;
                private String productCustomLabel12;
                private String productCustomLabel13;
                private String productCustomLabel14;
                private String productCustomLabel15;
                private String productCustomLabel16;
                private String productCustomLabel17;
                private String productCustomLabel18;
                private String productCustomLabel19;
                private String productCustomLabel20;
                private String productCustomLabel21;
                private String productCustomLabel22;
                private String productCustomLabel23;
                private String productCustomLabel24;
                private String productCustomLabel25;
                private String productCustomLabel26;
                private String productCustomLabel27;
                private String productCustomLabel28;
                private String productCustomLabel29;
                private String productCustomLabel30;
                private String productCustomLabel31;
                private String productCustomLabel32;
                private String productCustomLabel33;
                private String productCustomLabel34;
                private String productCustomLabel35;
                private String productCustomLabel36;
                private String productCustomLabel37;
                private String productCustomLabel38;
                private String productCustomLabel39;
                private String productCustomLabel40;
                private String productCustomLabel41;
                private String productCustomLabel42;
                private String productCustomLabel43;
                private String productCustomLabel44;
                private String productCustomLabel45;
                private String productCustomLabel46;
                private String productCustomLabel47;
                private String productCustomLabel48;
                private String productCustomLabel49;
                private String productCustomLabel50;
                private String productCustomLabel51;
                private String productCustomLabel52;
                private String productCustomLabel53;
                private String productCustomLabel54;
                private String productCustomLabel55;
                private String productCustomLabel56;
                private String productCustomLabel57;
                private String productCustomLabel58;
                private String productCustomLabel59;
                private String productCustomLabel60;
                private String productCustomLabel61;
                private String productCustomLabel62;
                private String productCustomLabel63;
                private String productCustomLabel64;
                private String productCustomLabel65;
                private String productCustomLabel66;
                private String productCustomLabel67;
                private String productCustomLabel68;
                private String productCustomLabel69;
                private String productCustomLabel70;
                private String productCustomLabel71;
                private String productCustomLabel72;
                private String productCustomLabel73;
                private String productCustomLabel74;
                private String productCustomLabel75;
                private String productCustomLabel76;
                private String productCustomLabel77;
                private String productCustomLabel78;
                private String productCustomLabel79;
                private String productCustomLabel80;
                private String productCustomLabel81;
                private String productCustomLabel82;
                private String productCustomLabel83;
                private String productCustomLabel84;
                private String productCustomLabel85;
                private String productCustomLabel86;
                private String productCustomLabel87;
                private String productCustomLabel88;
                private String productCustomLabel89;
                private String productCustomLabel90;
                private String productCustomLabel91;
                private String productCustomLabel92;
                private String productCustomLabel93;
                private String productCustomLabel94;
                private String productCustomLabel95;
                private String productCustomLabel96;
                private String productCustomLabel97;
                private String productCustomLabel98;
                private String productCustomLabel99;
                private String productCustomLabel100;
            }
            
            @Data
            @Builder
            @NoArgsConstructor
            @AllArgsConstructor
            public static class ButtonDto {
                private String type;
                private String url;
                private String title;
                private String webviewHeightRatio;
                private String messengerExtensions;
                private String fallbackUrl;
                private String webviewShareButton;
                private String phoneNumber;
                private String payload;
            }
            
            @Data
            @Builder
            @NoArgsConstructor
            @AllArgsConstructor
            public static class ElementDto {
                private String title;
                private String subtitle;
                private String imageUrl;
                private List<ButtonDto> buttons;
                private String itemUrl;
                private DefaultActionDto defaultAction;
                private QuantityDto quantity;
                private PriceDto price;
                private CurrencyDto currency;
                private SubtitleTextDto subtitleText;
                private TitleTextDto titleText;
                private ImageUrlTextDto imageUrlText;
                private ButtonsTextDto buttonsText;
                private ItemUrlTextDto itemUrlText;
                private DefaultActionTextDto defaultActionText;
                private QuantityTextDto quantityText;
                private PriceTextDto priceText;
                private CurrencyTextDto currencyText;
                
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class DefaultActionDto {
                    private String type;
                    private String url;
                    private String webviewHeightRatio;
                    private String messengerExtensions;
                    private String fallbackUrl;
                    private String webviewShareButton;
                }
                
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class QuantityDto {
                    private String quantity;
                }
                
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class PriceDto {
                    private String amount;
                    private String currency;
                }
                
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class CurrencyDto {
                    private String currency;
                }
                
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class SubtitleTextDto {
                    private String subtitle;
                }
                
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class TitleTextDto {
                    private String title;
                }
                
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class ImageUrlTextDto {
                    private String imageUrl;
                }
                
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class ButtonsTextDto {
                    private List<ButtonDto> buttons;
                }
                
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class ItemUrlTextDto {
                    private String itemUrl;
                }
                
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class DefaultActionTextDto {
                    private DefaultActionDto defaultAction;
                }
                
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class QuantityTextDto {
                    private QuantityDto quantity;
                }
                
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class PriceTextDto {
                    private PriceDto price;
                }
                
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class CurrencyTextDto {
                    private CurrencyDto currency;
                }
            }
            
            @Data
            @Builder
            @NoArgsConstructor
            @AllArgsConstructor
            public static class QuickReplyDto {
                private String title;
                private String payload;
                private String contentType;
                private ImageUrlDto imageUrl;
                
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class ImageUrlDto {
                    private String imageUrl;
                }
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
                private String merchantId;
                private String productId;
                private String productRetailerId;
                private String title;
                private String description;
                private String price;
                private String currency;
                private String imageUrl;
                private String url;
                private String condition;
                private String availability;
                private String brand;
                private String category;
                private String googleProductCategory;
                private String gtin;
                private String mpn;
                private String size;
                private String color;
                private String gender;
                private String ageGroup;
                private String material;
                private String pattern;
                private String shippingWeight;
                private String shippingWeightUnit;
                private String groupId;
                private String itemGroupId;
                private String adult;
                private List<String> customLabel;
            }
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class QuickReplyDto {
            private String title;
            private String payload;
            private String contentType;
            private ImageUrlDto imageUrl;
            
            @Data
            @Builder
            @NoArgsConstructor
            @AllArgsConstructor
            public static class ImageUrlDto {
                private String imageUrl;
            }
        }
    }
}
