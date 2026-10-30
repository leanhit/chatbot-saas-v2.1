package com.chatbot.core.customer.controller;

import com.chatbot.core.customer.dto.*;
import com.chatbot.core.customer.service.FeedbackService;
import com.chatbot.core.tenant.infra.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/customer/feedback")
@RequiredArgsConstructor
@Slf4j
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<FeedbackDTO> createFeedback(@RequestBody CreateFeedbackRequest request) {
        Long tenantId = TenantContext.getTenantId();
        FeedbackDTO created = feedbackService.createFeedback(request, tenantId);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedbackDTO> getFeedback(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        FeedbackDTO feedback = feedbackService.getFeedbackById(id, tenantId);
        return ResponseEntity.ok(feedback);
    }

    @GetMapping
    public ResponseEntity<List<FeedbackDTO>> getAllFeedback() {
        Long tenantId = TenantContext.getTenantId();
        List<FeedbackDTO> feedbackList = feedbackService.getAllFeedback(tenantId);
        return ResponseEntity.ok(feedbackList);
    }

    @GetMapping("/customer/{customerProfileId}")
    public ResponseEntity<List<FeedbackDTO>> getFeedbackByCustomer(@PathVariable Long customerProfileId) {
        Long tenantId = TenantContext.getTenantId();
        List<FeedbackDTO> feedbackList = feedbackService.getFeedbackByCustomerProfile(customerProfileId, tenantId);
        return ResponseEntity.ok(feedbackList);
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<FeedbackDTO>> getFeedbackByConversation(@PathVariable Long conversationId) {
        Long tenantId = TenantContext.getTenantId();
        List<FeedbackDTO> feedbackList = feedbackService.getFeedbackByConversation(conversationId, tenantId);
        return ResponseEntity.ok(feedbackList);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<FeedbackDTO>> getFeedbackByStatus(@PathVariable String status) {
        Long tenantId = TenantContext.getTenantId();
        List<FeedbackDTO> feedbackList = feedbackService.getFeedbackByStatus(tenantId, status);
        return ResponseEntity.ok(feedbackList);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<FeedbackDTO>> getFeedbackByType(@PathVariable String type) {
        Long tenantId = TenantContext.getTenantId();
        List<FeedbackDTO> feedbackList = feedbackService.getFeedbackByType(tenantId, type);
        return ResponseEntity.ok(feedbackList);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<FeedbackDTO>> getFeedbackByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Long tenantId = TenantContext.getTenantId();
        List<FeedbackDTO> feedbackList = feedbackService.getFeedbackByDateRange(tenantId, startDate, endDate);
        return ResponseEntity.ok(feedbackList);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<FeedbackDTO>> getRecentFeedback(@RequestParam(defaultValue = "10") int limit) {
        Long tenantId = TenantContext.getTenantId();
        List<FeedbackDTO> feedbackList = feedbackService.getRecentFeedback(tenantId, limit);
        return ResponseEntity.ok(feedbackList);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeedbackDTO> updateFeedback(
            @PathVariable Long id,
            @RequestBody UpdateFeedbackRequest request) {
        Long tenantId = TenantContext.getTenantId();
        String reviewedBy = getCurrentUserId();
        FeedbackDTO updated = feedbackService.updateFeedback(id, request, tenantId, reviewedBy);
        return ResponseEntity.ok(updated);
    }

    private String getCurrentUserId() {
        org.springframework.security.core.Authentication authentication =
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
            !authentication.getName().equals("anonymousUser")) {
            return authentication.getName();
        }
        return "system";
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        feedbackService.deleteFeedback(id, tenantId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/customer/{customerProfileId}/satisfaction")
    public ResponseEntity<Void> updateCustomerSatisfaction(@PathVariable Long customerProfileId) {
        Long tenantId = TenantContext.getTenantId();
        feedbackService.updateCustomerSatisfactionScore(customerProfileId, tenantId);
        return ResponseEntity.ok().build();
    }
}
