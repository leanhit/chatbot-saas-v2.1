package com.chatbot.spokes.facebook.connection.model;

/**
 * Enum định nghĩa các loại chatbot provider
 */
public enum ChatbotProvider {
    BOTPRESS("Botpress"),
    RASA("Rasa"),
    DIALOGFLOW("Dialogflow"),
    CUSTOM("Custom");

    private final String displayName;

    ChatbotProvider(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
