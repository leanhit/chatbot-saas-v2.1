package com.chatbot.core.user.service;

import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled Worker for purging soft-deleted / inactive users and expired user data
 * Ensures database integrity and soft-delete data lifecycle management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserCleanupService {

    private final UserRepository userRepository;

    /**
     * Scheduled job to purge inactive users disabled for more than 30 days
     * Runs daily at 3 AM with ShedLock distributed locking
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @SchedulerLock(name = "UserCleanupService_scheduledCleanup", lockAtMostFor = "1h", lockAtLeastFor = "50m")
    @Transactional(value = "userTransactionManager", rollbackFor = Exception.class)
    public void scheduledUserCleanup() {
        log.info("[UserCleanupService] Starting scheduled purge for inactive users");
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);

        try {
            List<User> inactiveUsers = userRepository.findInactiveUsersOlderThan(cutoffDate);
            if (inactiveUsers.isEmpty()) {
                log.info("[UserCleanupService] No inactive users found for cleanup (older than 30 days)");
                return;
            }

            log.info("[UserCleanupService] Found {} inactive users eligible for cleanup", inactiveUsers.size());
            int count = 0;
            for (User user : inactiveUsers) {
                try {
                    log.info("[UserCleanupService] Purging soft-deleted user: {} (ID: {})", user.getEmail(), user.getId());
                    userRepository.delete(user);
                    count++;
                } catch (Exception e) {
                    log.error("[UserCleanupService] Error purging user ID {}: {}", user.getId(), e.getMessage());
                }
            }

            log.info("[UserCleanupService] Completed user cleanup - Purged {} users", count);
        } catch (Exception e) {
            log.error("[UserCleanupService] Error executing scheduled user cleanup", e);
        }
    }
}
