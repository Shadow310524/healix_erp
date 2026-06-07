package in.healix.security.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.UUID;

public class HealixUserPrincipal extends User {
    private final UUID tenantId;
    private final UUID userId;

    public HealixUserPrincipal(String username, String password, UUID tenantId, UUID userId,
                               Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.tenantId = tenantId;
        this.userId = userId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getUserId() {
        return userId;
    }
}
