package com.chatbot.spokes.facebook.webhook.model;

/**
 * Facebook Message Types
 * Following traloitudongV2 pattern
 */
public enum FacebookMessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    FILE,
    ATTACHMENT,
    QUICK_REPLY,
    POSTBACK,
    REACTION,
    READ,
    DELIVERY,
    ECHO,
    UNKNOWN
}
