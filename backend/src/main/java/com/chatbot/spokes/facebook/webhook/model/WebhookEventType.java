package com.chatbot.spokes.facebook.webhook.model;

/**
 * Webhook Event Types
 */
public enum WebhookEventType {
    MESSAGE("message"),
    POSTBACK("postback"),
    OPTIN("optin"),
    REFERRAL("referral"),
    DELIVERY("delivery"),
    READ("read"),
    ACCOUNT_LINKING("account_linking"),
    UNSUBSCRIBE("unsubscribe");
    
    private final String value;
    
    WebhookEventType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
