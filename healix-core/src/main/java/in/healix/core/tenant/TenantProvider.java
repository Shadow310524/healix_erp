package in.healix.core.tenant;

import java.util.Optional;
import java.util.UUID;

public interface TenantProvider {
    Optional<UUID> getTenantId();
}
