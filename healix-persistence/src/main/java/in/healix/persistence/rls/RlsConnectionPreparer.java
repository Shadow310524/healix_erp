package in.healix.persistence.rls;

import in.healix.core.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
public class RlsConnectionPreparer {
    private static final Logger log = LoggerFactory.getLogger(RlsConnectionPreparer.class);

    public void prepare(Connection connection) throws SQLException {
        String tenantId = TenantContext.getTenantId();
        
        if (log.isTraceEnabled()) {
            log.trace("Applying PostgreSQL RLS setting app.current_tenant_id = '{}'", tenantId);
        }

        // Secure, parameterized set_config call to prevent SQL injection
        String sql = "SELECT set_config('app.current_tenant_id', ?, true)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (tenantId != null && !tenantId.trim().isEmpty()) {
                ps.setString(1, tenantId);
            } else {
                ps.setString(1, "");
            }
            ps.execute();
        }
    }
}
