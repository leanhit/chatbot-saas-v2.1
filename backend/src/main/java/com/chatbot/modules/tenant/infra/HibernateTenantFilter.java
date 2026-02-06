package com.chatbot.modules.tenant.infra;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

/**
 * Hibernate Tenant Filter
 * Applies tenant filtering to Hibernate queries
 * 
 * v0.2: Updated to use UUID tenant IDs
 */
@Component
@RequestScope
public class HibernateTenantFilter {

    @PersistenceContext
    private EntityManager entityManager;

    public void enable() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }

        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("tenantFilter")
               .setParameter("tenantId", tenantId);
    }
}
