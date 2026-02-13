package com.chatbot.core.app.guard.repository;

import com.chatbot.core.app.guard.model.GuardRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuardRuleRepository extends JpaRepository<GuardRule, Long> {
    
    List<GuardRule> findByAppGuardId(Long appGuardId);
    
    List<GuardRule> findByAppGuardIdAndIsActive(Long appGuardId, Boolean isActive);
    
    Optional<GuardRule> findByAppGuardIdAndRuleName(Long appGuardId, String ruleName);
    
    boolean existsByAppGuardIdAndRuleName(Long appGuardId, String ruleName);
    
    boolean existsByAppGuardIdAndRuleNameAndIdNot(Long appGuardId, String ruleName, Long id);
}
