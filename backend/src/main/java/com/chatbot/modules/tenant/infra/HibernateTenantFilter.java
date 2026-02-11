package com.chatbot.modules.tenant.infra;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class HibernateTenantFilter {

    @PersistenceContext
    private EntityManager entityManager;

    public void enable() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }

        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("tenantFilter")
               .setParameter("tenantId", tenantId);
    }
}
