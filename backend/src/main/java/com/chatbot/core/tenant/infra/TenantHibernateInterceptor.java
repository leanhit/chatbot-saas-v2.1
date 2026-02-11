package com.chatbot.core.tenant.infra;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * Hibernate interceptor for automatic tenant filtering
 */
@Slf4j
public class TenantHibernateInterceptor extends EmptyInterceptor {
    
    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if (entity instanceof BaseTenantEntity) {
            BaseTenantEntity tenantEntity = (BaseTenantEntity) entity;
            String currentTenant = TenantContext.getCurrentTenant();
            
            if (currentTenant != null && tenantEntity.getTenantKey() == null) {
                tenantEntity.setTenantKey(currentTenant);
                log.debug("Set tenant key {} for entity {}", currentTenant, entity.getClass().getSimpleName());
            }
        }
        return super.onSave(entity, id, state, propertyNames, types);
    }
    
    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if (entity instanceof BaseTenantEntity) {
            BaseTenantEntity tenantEntity = (BaseTenantEntity) entity;
            tenantEntity.softDelete();
            log.debug("Soft deleted entity {} with tenant {}", entity.getClass().getSimpleName(), tenantEntity.getTenantKey());
        }
        super.onDelete(entity, id, state, propertyNames, types);
    }
}
