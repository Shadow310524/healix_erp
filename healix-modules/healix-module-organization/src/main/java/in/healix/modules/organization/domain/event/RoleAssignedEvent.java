package in.healix.modules.organization.domain.event;

import java.util.UUID;

public record RoleAssignedEvent(
    UUID eventId,
    UUID tenantId,
    UUID userId,
    UUID roleId,
    String roleName
) {}
