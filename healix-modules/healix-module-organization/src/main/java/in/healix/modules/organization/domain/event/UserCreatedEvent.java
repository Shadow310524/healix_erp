package in.healix.modules.organization.domain.event;

import java.util.UUID;

public record UserCreatedEvent(
    UUID eventId,
    UUID tenantId,
    UUID userId,
    String email,
    String firstName,
    String lastName
) {}
