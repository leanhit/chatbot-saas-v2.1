package com.platform.core.skeleton;

/**
 * Backend Skeleton Marker Class
 * 
 * This class serves as a structural marker for the locked Backend Skeleton of the Hub & Spoke SaaS platform.
 * 
 * <p><strong>IMPORTANT:</strong> This class contains NO business logic and MUST NOT be modified
 * without explicit architecture approval. Any changes to the Backend Skeleton require
 * formal architecture decision and coordination with the server version team.</p>
 * 
 * <p><strong>What is marked as Backend Skeleton:</strong></p>
 * <ul>
 *   <li>Core tenant management (Tenant Hub v0.1)</li>
 *   <li>Authentication and authorization framework</li>
 *   <li>App service guard framework</li>
 *   <li>Hub & Spoke architecture boundaries</li>
 *   <li>Core data models and repositories</li>
 * </ul>
 * 
 * <p><strong>What is explicitly excluded:</strong></p>
 * <ul>
 *   <li>Business logic for specific applications</li>
 *   <li>Payment and billing implementations</li>
 *   <li>External service integrations</li>
 *   <li>Feature flags or runtime configuration</li>
 *   <li>Any mutable state or behavior</li>
 * </ul>
 * 
 * <p>This marker ensures the Backend Skeleton remains stable and can be safely merged
 * with server-side implementations without conflicts.</p>
 * 
 * @author Platform Architecture Team
 * @version 1.0
 * @since Backend Skeleton Lock
 */
public final class BackendSkeleton {
    
    /**
     * Private constructor to prevent instantiation.
     * This is a marker class only and should never be instantiated.
     */
    private BackendSkeleton() {
        throw new UnsupportedOperationException("BackendSkeleton is a marker class and cannot be instantiated");
    }
}
