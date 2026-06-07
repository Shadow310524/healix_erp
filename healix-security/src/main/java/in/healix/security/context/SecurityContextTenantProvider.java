package in.healix.security.context;

import in.healix.core.tenant.TenantProvider;
import in.healix.security.auth.HealixUserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SecurityContextTenantProvider implements TenantProvider {

    @Override
    public Optional<UUID> getTenantId() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> {
                    if (auth.getPrincipal() instanceof HealixUserPrincipal principal) {
                        return principal.getTenantId();
                    }
                    return null;
                });
    }
}
