package in.healix.core.tenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenantContext {
    private static final Logger log = LoggerFactory.getLogger(TenantContext.class);
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        if (log.isTraceEnabled()) {
            log.trace("Setting tenant context to: {}", tenantId);
        }
        CURRENT_TENANT.set(tenantId);
    }

    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        if (log.isTraceEnabled()) {
            log.trace("Clearing tenant context");
        }
        CURRENT_TENANT.remove();
    }
}
