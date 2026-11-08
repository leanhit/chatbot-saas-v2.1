package com.chatbot.core.routing.controller;

import com.chatbot.core.routing.dto.*;
import com.chatbot.core.routing.model.*;
import com.chatbot.core.routing.service.IntelligentRoutingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RoutingControllerIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private IntelligentRoutingService routingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        IntelligentRoutingController controller = new IntelligentRoutingController(routingService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new com.chatbot.shared.exceptions.GlobalExceptionHandler())
                .build();
    }

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
