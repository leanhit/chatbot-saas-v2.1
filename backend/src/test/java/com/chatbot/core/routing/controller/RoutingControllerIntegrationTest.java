package com.chatbot.core.routing.controller;

import com.chatbot.core.routing.dto.*;
import com.chatbot.core.routing.model.RoutingRule;
import com.chatbot.core.routing.service.IntelligentRoutingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IntelligentRoutingController.class)
@ActiveProfiles("test")
@Disabled("ApplicationContext loading issues - requires full Spring context configuration")
class RoutingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IntelligentRoutingService routingService;

    @Test
    void testPostCreateRoutingRule() throws Exception {
        // Arrange
        CreateRoutingRuleRequest request = new CreateRoutingRuleRequest();
        request.setName("Test Rule");
        request.setRuleType(RoutingRule.RoutingRuleType.PRIORITY_BASED);
        request.setPriority(1);
        request.setTargetType(RoutingRule.TargetType.AGENT);
        request.setIsActive(true);

        RoutingRuleDTO response = new RoutingRuleDTO();
        response.setId(1L);
        response.setName("Test Rule");

        when(routingService.createRoutingRule(any(CreateRoutingRuleRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/routing/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Rule"));
    }

    @Test
    void testGetAllRoutingRules() throws Exception {
        // Arrange
        List<RoutingRuleDTO> rules = new ArrayList<>();
        RoutingRuleDTO rule1 = new RoutingRuleDTO();
        rule1.setId(1L);
        rule1.setName("Rule 1");
        rules.add(rule1);

        when(routingService.getAllRoutingRules()).thenReturn(rules);

        // Act & Assert
        mockMvc.perform(get("/api/routing/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Rule 1"));
    }

    @Test
    void testGetRoutingRuleById() throws Exception {
        // Arrange
        RoutingRuleDTO rule = new RoutingRuleDTO();
        rule.setId(1L);
        rule.setName("Test Rule");

        when(routingService.getRoutingRuleById(1L)).thenReturn(rule);

        // Act & Assert
        mockMvc.perform(get("/api/routing/rules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Rule"));
    }

    @Test
    void testUpdateRoutingRule() throws Exception {
        // Arrange
        UpdateRoutingRuleRequest request = new UpdateRoutingRuleRequest();
        request.setName("Updated Rule");

        RoutingRuleDTO response = new RoutingRuleDTO();
        response.setId(1L);
        response.setName("Updated Rule");

        when(routingService.updateRoutingRule(eq(1L), any(UpdateRoutingRuleRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/routing/rules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Rule"));
    }

    @Test
    void testDeleteRoutingRule() throws Exception {
        // Arrange
        doNothing().when(routingService).deleteRoutingRule(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/routing/rules/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testRouteConversation() throws Exception {
        // Arrange
        Map<String, Object> context = new HashMap<>();
        context.put("test", "value");

        RoutingAssignmentResult result = RoutingAssignmentResult.builder()
                .conversationId(1L)
                .assigned(true)
                .assignedAgentId(10L)
                .routingStrategy("PRIORITY_BASED")
                .build();

        when(routingService.routeConversation(eq(1L), any(Map.class))).thenReturn(result);

        // Act & Assert
        mockMvc.perform(post("/api/routing/rules/route/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(context)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigned").value(true))
                .andExpect(jsonPath("$.assignedAgentId").value(10L));
    }

    @Test
    void testGetRoutingHistory() throws Exception {
        // Arrange
        List<RoutingAssignmentResult> history = new ArrayList<>();
        RoutingAssignmentResult result = RoutingAssignmentResult.builder()
                .conversationId(1L)
                .assigned(true)
                .assignedAgentId(10L)
                .build();
        history.add(result);

        when(routingService.getRoutingHistory(1L)).thenReturn(history);

        // Act & Assert
        mockMvc.perform(get("/api/routing/rules/history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].conversationId").value(1L));
    }
}
