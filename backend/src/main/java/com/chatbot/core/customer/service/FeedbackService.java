package com.chatbot.core.customer.service;

import com.chatbot.core.customer.dto.*;
import com.chatbot.core.customer.model.Feedback;
import com.chatbot.core.customer.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    public FeedbackDTO createFeedback(CreateFeedbackRequest request, Long tenantId) {
        log.info("Creating feedback for customerProfileId: {}, tenantId: {}", request.getCustomerProfileId(), tenantId);

        Feedback feedback = Feedback.builder()
                .customerProfileId(request.getCustomerProfileId())
                .conversationId(request.getConversationId())
                .type(request.getType() != null ? Feedback.FeedbackType.valueOf(request.getType()) : Feedback.FeedbackType.GENERAL)
                .source(request.getSource() != null ? Feedback.FeedbackSource.valueOf(request.getSource()) : Feedback.FeedbackSource.WEBCHAT)
                .rating(request.getRating())
                .comment(request.getComment())
                .category(request.getCategory())
                .isAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false)
                .submittedAt(LocalDateTime.now())
                .status(Feedback.FeedbackStatus.PENDING)
                .build();

        feedback.setTenantId(tenantId);

        Feedback saved = feedbackRepository.save(feedback);
        log.info("Feedback created with id: {}", saved.getId());
        return toDTO(saved);
    }

    public FeedbackDTO getFeedbackById(Long id, Long tenantId) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found with id: " + id));

        if (!feedback.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Access denied: Feedback does not belong to tenant");
        }

        return toDTO(feedback);
    }

    public List<FeedbackDTO> getAllFeedback(Long tenantId) {
        List<Feedback> feedbackList = feedbackRepository.findByTenantId(tenantId);
        return feedbackList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<FeedbackDTO> getFeedbackByCustomerProfile(Long customerProfileId, Long tenantId) {
        List<Feedback> feedbackList = feedbackRepository.findByCustomerProfileIdAndTenantId(customerProfileId, tenantId);
        return feedbackList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<FeedbackDTO> getFeedbackByConversation(Long conversationId, Long tenantId) {
        List<Feedback> feedbackList = feedbackRepository.findByConversationIdAndTenantId(conversationId, tenantId);
        return feedbackList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<FeedbackDTO> getFeedbackByStatus(Long tenantId, String status) {
        List<Feedback> feedbackList = feedbackRepository.findByTenantIdAndStatus(
                tenantId, Feedback.FeedbackStatus.valueOf(status));
        return feedbackList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<FeedbackDTO> getFeedbackByType(Long tenantId, String type) {
        List<Feedback> feedbackList = feedbackRepository.findByTenantIdAndType(
                tenantId, Feedback.FeedbackType.valueOf(type));
        return feedbackList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<FeedbackDTO> getFeedbackByDateRange(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Feedback> feedbackList = feedbackRepository.findByTenantIdAndSubmittedAtBetween(tenantId, startDate, endDate);
        return feedbackList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<FeedbackDTO> getRecentFeedback(Long tenantId, int limit) {
        List<Feedback> feedbackList = feedbackRepository.findByTenantIdOrderBySubmittedAtDesc(tenantId);
        return feedbackList.stream()
                .limit(limit)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public FeedbackDTO updateFeedback(Long id, UpdateFeedbackRequest request, Long tenantId, String reviewedBy) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found with id: " + id));

        if (!feedback.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Access denied: Feedback does not belong to tenant");
        }

        if (request.getStatus() != null) {
            feedback.setStatus(Feedback.FeedbackStatus.valueOf(request.getStatus()));
            if ("REVIEWED".equals(request.getStatus()) || "RESPONDED".equals(request.getStatus())) {
                feedback.setReadAt(LocalDateTime.now());
                feedback.setReviewedBy(reviewedBy);
            }
        }

        if (request.getAdminResponse() != null) {
            feedback.setAdminResponse(request.getAdminResponse());
            feedback.setResponseAt(LocalDateTime.now());
            if (feedback.getStatus() == Feedback.FeedbackStatus.REVIEWED) {
                feedback.setStatus(Feedback.FeedbackStatus.RESPONDED);
            }
        }

        Feedback saved = feedbackRepository.save(feedback);
        log.info("Feedback updated with id: {}", saved.getId());
        return toDTO(saved);
    }

    @Transactional
    public void deleteFeedback(Long id, Long tenantId) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found with id: " + id));

        if (!feedback.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Access denied: Feedback does not belong to tenant");
        }

        feedbackRepository.delete(feedback);
        log.info("Feedback deleted with id: {}", id);
    }

    @Transactional
    public void updateCustomerSatisfactionScore(Long customerProfileId, Long tenantId) {
        List<Feedback> feedbackList = feedbackRepository.findByCustomerProfileIdAndTenantId(customerProfileId, tenantId);
        
        if (feedbackList.isEmpty()) {
            return;
        }

        double avgRating = feedbackList.stream()
                .filter(f -> f.getRating() != null)
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);

        // Note: This would require CustomerProfileRepository injection
        // For now, just log the calculation
        log.info("Calculated satisfaction score for customerProfileId {}: {}", customerProfileId, avgRating);
    }

    private FeedbackDTO toDTO(Feedback feedback) {
        FeedbackDTO dto = new FeedbackDTO();
        dto.setId(feedback.getId());
        dto.setCustomerProfileId(feedback.getCustomerProfileId());
        dto.setConversationId(feedback.getConversationId());
        dto.setType(feedback.getType() != null ? feedback.getType().name() : null);
        dto.setSource(feedback.getSource() != null ? feedback.getSource().name() : null);
        dto.setRating(feedback.getRating());
        dto.setComment(feedback.getComment());
        dto.setCategory(feedback.getCategory());
        dto.setIsAnonymous(feedback.getIsAnonymous());
        dto.setSubmittedAt(feedback.getSubmittedAt());
        dto.setReviewedAt(feedback.getReadAt());
        dto.setReviewedBy(feedback.getReviewedBy());
        dto.setStatus(feedback.getStatus() != null ? feedback.getStatus().name() : null);
        dto.setAdminResponse(feedback.getAdminResponse());
        dto.setResponseAt(feedback.getResponseAt());
        return dto;
    }
}
