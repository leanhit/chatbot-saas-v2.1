package com.chatbot.core.message.decision.model;

import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

/**
 * Unit tests for TakeoverMessage validation
 */
public class TakeoverMessageValidationTest {

    private final Validator validator;

    public TakeoverMessageValidationTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidTakeoverMessage() {
        TakeoverMessage message = new TakeoverMessage(
            "msg123",
            "conv456",
            "agent",
            "Hello world",
            System.currentTimeMillis()
        );

        Set<ConstraintViolation<TakeoverMessage>> violations = validator.validate(message);
        assertTrue(violations.isEmpty(), "Valid message should have no violations");
    }

    @Test
    public void testBlankMessageIdAllowed() {
        TakeoverMessage message = new TakeoverMessage(
            "",
            "conv456",
            "agent",
            "Hello world",
            System.currentTimeMillis()
        );

        Set<ConstraintViolation<TakeoverMessage>> violations = validator.validate(message);
        assertTrue(violations.isEmpty(), "Blank message ID should be allowed");
    }

    @Test
    public void testBlankConversationId() {
        TakeoverMessage message = new TakeoverMessage(
            "msg123",
            "",
            "agent",
            "Hello world",
            System.currentTimeMillis()
        );

        Set<ConstraintViolation<TakeoverMessage>> violations = validator.validate(message);
        assertFalse(violations.isEmpty(), "Blank conversation ID should have violations");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Conversation ID is required")));
    }

    @Test
    public void testBlankSenderAllowed() {
        TakeoverMessage message = new TakeoverMessage(
            "msg123",
            "conv456",
            "",
            "Hello world",
            System.currentTimeMillis()
        );

        Set<ConstraintViolation<TakeoverMessage>> violations = validator.validate(message);
        assertTrue(violations.isEmpty(), "Blank sender should be allowed");
    }

    @Test
    public void testBlankContent() {
        TakeoverMessage message = new TakeoverMessage(
            "msg123",
            "conv456",
            "agent",
            "",
            System.currentTimeMillis()
        );

        Set<ConstraintViolation<TakeoverMessage>> violations = validator.validate(message);
        assertFalse(violations.isEmpty(), "Blank content should have violations");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Content is required")));
    }

    @Test
    public void testNullTimestampAllowed() {
        TakeoverMessage message = new TakeoverMessage(
            "msg123",
            "conv456",
            "agent",
            "Hello world",
            null
        );

        Set<ConstraintViolation<TakeoverMessage>> violations = validator.validate(message);
        assertTrue(violations.isEmpty(), "Null timestamp should be allowed");
    }
}
