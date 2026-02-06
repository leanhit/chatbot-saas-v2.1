package com.chatbot.modules.app.core.guard;

import com.chatbot.modules.app.core.model.AppCode;
import com.chatbot.modules.facebook.facebook.connection.model.FacebookConnection;
import com.chatbot.modules.facebook.facebook.connection.repository.FacebookConnectionRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * AppServiceGuard for SEND_MESSAGE
 *
 * Rules:
 * 1. Tenant must exist
 * 2. FacebookConnection must exist
 * 3. FacebookConnection must be enabled
 *
 * FAIL  -> throw GuardException
 * PASS  -> return GuardPassContext
 */
@Service
public class SendMessageGuard implements AppServiceGuard {

    private final FacebookConnectionRepository connectionRepository;

    public SendMessageGuard(FacebookConnectionRepository connectionRepository) {
        this.connectionRepository = connectionRepository;
    }

    @Override
    public GuardPassContext check(GuardRequest request) {

        // 1. Tenant must exist
        if (request.getTenantId() == null) {
            throw new GuardException("Tenant not resolved");
        }

        // TODO: Extract pageId from command context - for now using stub
        String pageId = "stub_page_id"; // This should come from command context

        // 2. FacebookConnection must exist
        Optional<FacebookConnection> connectionOpt =
                connectionRepository.findByTenantIdAndPageId(
                        UUID.fromString("00000000-0000-0000-0000-000000000001"), // Stub UUID tenantId for now
                        pageId
                );

        if (connectionOpt.isEmpty()) {
            throw new GuardException("FacebookConnection not found");
        }

        FacebookConnection connection = connectionOpt.get();

        // 3. FacebookConnection must be enabled
        if (!connection.isEnabled()) {
            throw new GuardException("FacebookConnection is disabled");
        }

        // PASS
        return GuardPassContext.builder()
                .billable(false)
                .cost(BigDecimal.ZERO)
                .build();
    }

    @Override
    public boolean supports(AppCode appCode) {
        return AppCode.SEND_MESSAGE.equals(appCode);
    }
}
