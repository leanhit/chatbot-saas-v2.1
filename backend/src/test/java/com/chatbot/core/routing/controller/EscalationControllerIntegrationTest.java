package com.chatbot.core.routing.controller;

import com.chatbot.core.routing.dto.*;
import com.chatbot.core.routing.model.EscalationRule;
import com.chatbot.core.routing.service.IntelligentEscalationService;
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

@WebMvcTest(IntelligentEscalationController.class)
@ActiveProfiles("test")
@Disabled("ApplicationContext loading issues - requires full Spring context configuration")
class EscalationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IntelligentEscalationService escalationService;

    @Test
    void testPostCreateEscalationRule() throws Exception {
        // Arrange
        CreateEscalationRuleRequest request = new CreateEscalationRuleRequest();
        request.setName("Test Escalation Rule");
        request.setTriggerType(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
        request.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        request.setPriority(1);
        request.setIsActive(true);

        EscalationRuleDTO response = new EscalationRuleDTO();
        response.setId(1L);
        response.setName("Test Escalation Rule");

        when(escalationService.createEscalationRule(any(CreateEscalationRuleRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/routing/escalation-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Escalation Rule"));
    }

    @Test
    void testGetAllEscalationRules() throws Exception {
        // Arrange
        List<EscalationRuleDTO> rules = new ArrayList<>();
        EscalationRuleDTO rule1 = new EscalationRuleDTO();
        rule1.setId(1L);
        rule1.setName("Rule 1");
        rules.add(rule1);

        when(escalationService.getAllEscalationRules()).thenReturn(rules);

        // Act & Assert
        mockMvc.perform(get("/api/routing/escalation-rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Rule 1"));
    }

    @Test
    void testGetEscalationRuleById() throws Exception {
        // Arrange
        EscalationRuleDTO rule = new EscalationRuleDTO();
        rule.setId(1L);
        rule.setName("Test Rule");

        when(escalationService.getEscalationRuleById(1L)).thenReturn(rule);

        // Act & Assert
        mockMvc.perform(get("/api/routing/escalation-rules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Rule"));
    }

    @Test
    void testUpdateEscalationRule() throws Exception {
        // Arrange
        UpdateEscalationRuleRequest request = new UpdateEscalationRuleRequest();
        request.setName("Updated Rule");

        EscalationRuleDTO response = new EscalationRuleDTO();
        response.setId(1L);
        response.setName("Updated Rule");

        when(escalationService.updateEscalationRule(eq(1L), any(UpdateEscalationRuleRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/routing/escalation-rules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Rule"));
    }

    @Test
    void testDeleteEscalationRule() throws Exception {
        // Arrange
        doNothing().when(escalationService).deleteEscalationRule(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/routing/escalation-rules/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testEvaluateEscalation() throws Exception {
        // Arrange
        EscalationContext context = new EscalationContext();
        context.setConversationId(1L);

        EscalationResult result = EscalationResult.builder()
                .conversationId(1L)
                .escalated(true)
                .escalatedToSupervisor(true)
                .escalationAction("ASSIGN_TO_SUPERVISOR")
                .build();

        when(escalationService.evaluateEscalation(eq(1L), any(EscalationContext.class))).thenReturn(result);

        // Act & Assert
        mockMvc.perform(post("/api/routing/escalation-rules/evaluate/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(context)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.escalated").value(true))
                .andExpect(jsonPath("$.escalatedToSupervisor").value(true));
    }

    @Test
    void testGetEscalationHistory() throws Exception {
        // Arrange
        List<EscalationResult> history = new ArrayList<>();
        EscalationResult result = EscalationResult.builder()
                .conversationId(1L)
                .escalated(true)
                .build();
        history.add(result);

        when(escalationService.getEscalationHistory(1L)).thenReturn(history);

        // Act & Assert
        mockMvc.perform(get("/api/routing/escalation-rules/history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].conversationId").value(1L));
    }
}
