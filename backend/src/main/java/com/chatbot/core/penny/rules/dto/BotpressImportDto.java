package com.chatbot.core.penny.rules.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * DTO for importing Botpress JSON format
 * Maps Botpress bot configuration to Penny custom logic
 */
@Data
public class BotpressImportDto {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("defaultLanguage")
    private String defaultLanguage;
    
    @JsonProperty("languages")
    private List<String> languages;
    
    @JsonProperty("details")
    private BotDetails details;
    
    @JsonProperty("imports")
    private Imports imports;
    
    @JsonProperty("messaging")
    private Messaging messaging;
    
    @JsonProperty("qna")
    private QnA qna;
    
    @Data
    public static class BotDetails {
        @JsonProperty("website")
        private String website;
        
        @JsonProperty("phoneNumber")
        private String phoneNumber;
        
        @JsonProperty("emailAddress")
        private String emailAddress;
    }
    
    @Data
    public static class Imports {
        @JsonProperty("contentTypes")
        private List<String> contentTypes;
    }
    
    @Data
    public static class Messaging {
        @JsonProperty("channels")
        private Map<String, Object> channels;
    }
    
    @Data
    public static class QnA {
        @JsonProperty("disabled")
        private Boolean disabled;
        
        @JsonProperty("items")
        private List<QnAItem> items;
    }
    
    @Data
    public static class QnAItem {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("questions")
        private List<String> questions;
        
        @JsonProperty("answers")
        private List<QnAAnswer> answers;
        
        @JsonProperty("enabled")
        private Boolean enabled;
    }
    
    @Data
    public static class QnAAnswer {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("text")
        private String text;
        
        @JsonProperty("metadata")
        private Map<String, Object> metadata;
    }
}
