package com.chatbot.modules.billing.core.model;

import com.chatbot.modules.app.core.model.AppCode;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

/**
 * Plan registry for v0.1
 * Static mapping of plans to allowed apps
 * This is a READ-ONLY policy service
 */
@Getter
public enum PlanRegistry {

    FREE_PLAN(PlanCode.FREE, EnumSet.of(AppCode.CHATBOT)),
    PRO_PLAN(PlanCode.PRO, EnumSet.of(AppCode.CHATBOT, AppCode.CRM)),
    ENTERPRISE_PLAN(PlanCode.ENTERPRISE, EnumSet.allOf(AppCode.class));

    private final PlanCode planCode;
    private final Set<AppCode> allowedApps;

    PlanRegistry(PlanCode planCode, Set<AppCode> allowedApps) {
        this.planCode = planCode;
        this.allowedApps = allowedApps;
    }

    /**
     * Check if plan allows specific app
     */
    public boolean allowsApp(AppCode appCode) {
        return allowedApps.contains(appCode);
    }

    /**
     * Get allowed apps for plan
     */
    public Set<AppCode> getAllowedApps() {
        return EnumSet.copyOf(allowedApps);
    }

    /**
     * Find registry by plan code
     */
    public static PlanRegistry findByPlanCode(PlanCode planCode) {
        for (PlanRegistry registry : values()) {
            if (registry.planCode == planCode) {
                return registry;
            }
        }
        throw new IllegalArgumentException("Unknown plan code: " + planCode);
    }

    /**
     * Check if plan allows app (static utility method)
     */
    public static boolean planAllowsApp(PlanCode planCode, AppCode appCode) {
        PlanRegistry registry = findByPlanCode(planCode);
        return registry.allowsApp(appCode);
    }
}
