package com.chatbot.core.routing.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConditionEvaluationTest {

    private IntelligentRoutingService routingService;

    @BeforeEach
    void setUp() {
        routingService = new IntelligentRoutingService(null, null, null, null, null);
    }

    @Test
    void testEvaluateConditions_NoConditions_ReturnsTrue() {
        Map<String, Object> conditions = null;
        Map<String, Object> context = new HashMap<>();

        boolean result = invokeEvaluateConditions(conditions, context);
        assertTrue(result);
    }

    @Test
    void testEvaluateConditions_EmptyConditions_ReturnsTrue() {
        Map<String, Object> conditions = new HashMap<>();
        Map<String, Object> context = new HashMap<>();

        boolean result = invokeEvaluateConditions(conditions, context);
        assertTrue(result);
    }

    @Test
    void testEvaluateConditions_SimpleEquality_Match() {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("priority", "high");

        Map<String, Object> context = new HashMap<>();
        context.put("priority", "high");

        boolean result = invokeEvaluateConditions(conditions, context);
        assertTrue(result);
    }

    @Test
    void testEvaluateConditions_SimpleEquality_NoMatch() {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("priority", "high");

        Map<String, Object> context = new HashMap<>();
        context.put("priority", "low");

        boolean result = invokeEvaluateConditions(conditions, context);
        assertFalse(result);
    }

    @Test
    void testEvaluateConditions_GreaterThan_Match() {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("age>18", 18);

        Map<String, Object> context = new HashMap<>();
        context.put("age", 25);

        boolean result = invokeEvaluateConditions(conditions, context);
        assertTrue(result);
    }

    @Test
    void testEvaluateConditions_GreaterThan_NoMatch() {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("age>18", 18);

        Map<String, Object> context = new HashMap<>();
        context.put("age", 15);

        boolean result = invokeEvaluateConditions(conditions, context);
        assertFalse(result);
    }

    @Test
    void testEvaluateConditions_LessThan_Match() {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("score<100", 100);

        Map<String, Object> context = new HashMap<>();
        context.put("score", 85);

        boolean result = invokeEvaluateConditions(conditions, context);
        assertTrue(result);
    }

    @Test
    void testEvaluateConditions_LessThan_NoMatch() {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("score<100", 100);

        Map<String, Object> context = new HashMap<>();
        context.put("score", 150);

        boolean result = invokeEvaluateConditions(conditions, context);
        assertFalse(result);
    }

    @Test
    void testEvaluateConditions_NotEqual_Match() {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("status!=offline", "offline");

        Map<String, Object> context = new HashMap<>();
        context.put("status", "online");

        boolean result = invokeEvaluateConditions(conditions, context);
        assertTrue(result);
    }

    @Test
    void testEvaluateConditions_NotEqual_NoMatch() {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("status!=offline", "offline");

        Map<String, Object> context = new HashMap<>();
        context.put("status", "offline");

        boolean result = invokeEvaluateConditions(conditions, context);
        assertFalse(result);
    }

    @Test
    void testEvaluateConditions_Contains_Match() {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("message contains help", "help");

        Map<String, Object> context = new HashMap<>();
        context.put("message", "I need help with my order");

        boolean result = invokeEvaluateConditions(conditions, context);
        assertTrue(result);
    }

    @Test
    void testEvaluateConditions_Contains_NoMatch() {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("message contains help", "help");

        Map<String, Object> context = new HashMap<>();
        context.put("message", "I want to place an order");

        boolean result = invokeEvaluateConditions(conditions, context);
        assertFalse(result);
    }

    @Test
    void testEvaluateConditions_Contains_CaseInsensitive() {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("message contains HELP", "HELP");

        Map<String, Object> context = new HashMap<>();
        context.put("message", "I need help with my order");

        boolean result = invokeEvaluateConditions(conditions, context);
        assertTrue(result);
    }

    @Test
    void testEvaluateConditions_MultipleConditions_AllMatch() {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("priority", "high");
        conditions.put("status", "online");
        conditions.put("age>18", 18);

        Map<String, Object> context = new HashMap<>();
        context.put("priority", "high");
        context.put("status", "online");
        context.put("age", 25);

        boolean result = invokeEvaluateConditions(conditions, context);
        assertTrue(result);
    }

    @Test
    void testEvaluateConditions_MultipleConditions_OneNoMatch() {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("priority", "high");
        conditions.put("status", "online");
        conditions.put("age>18", 18);

        Map<String, Object> context = new HashMap<>();
        context.put("priority", "high");
        context.put("status", "offline");
        context.put("age", 25);

        boolean result = invokeEvaluateConditions(conditions, context);
        assertFalse(result);
    }

    private boolean invokeEvaluateConditions(Map<String, Object> conditions, Map<String, Object> context) {
        return ReflectionTestUtils.invokeMethod(routingService, "evaluateConditions", conditions, context);
    }
}
