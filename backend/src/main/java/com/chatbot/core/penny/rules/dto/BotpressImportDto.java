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
    
    @JsonProperty("flows")
    private Flows flows;
    
    @JsonProperty("intents")
    private Intents intents;
    
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
    
    @Data
    public static class Flows {
        @JsonProperty("disabled")
        private Boolean disabled;
        
        @JsonProperty("items")
        private List<FlowItem> items;
    }
    
    @Data
    public static class FlowItem {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("nodes")
        private List<FlowNode> nodes;
        
        @JsonProperty("transitions")
        private List<FlowTransition> transitions;
    }
    
    @Data
    public static class FlowNode {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("content")
        private Map<String, Object> content;
    }
    
    @Data
    public static class FlowTransition {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("source")
        private String source;
        
        @JsonProperty("target")
        private String target;
        
        @JsonProperty("condition")
        private String condition;
    }
    
    @Data
    public static class Intents {
        @JsonProperty("disabled")
        private Boolean disabled;
        
        @JsonProperty("items")
        private List<IntentItem> items;
    }
    
    @Data
    public static class IntentItem {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("label")
        private String label;
        
        @JsonProperty("contexts")
        private List<String> contexts;
        
        @JsonProperty("utterances")
        private List<Utterance> utterances;
    }
    
    @Data
    public static class Utterance {
        @JsonProperty("text")
        private String text;
        
        @JsonProperty("entities")
        private List<Entity> entities;
    }
    
    @Data
    public static class Entity {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("value")
        private String value;
        
        @JsonProperty("meta")
        private Map<String, Object> meta;
    }
}
