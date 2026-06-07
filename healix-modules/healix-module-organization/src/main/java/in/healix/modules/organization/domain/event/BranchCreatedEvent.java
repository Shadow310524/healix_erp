package in.healix.modules.organization.domain.event;

import java.util.UUID;

public record BranchCreatedEvent(
    UUID eventId,
    UUID tenantId,
    UUID branchId,
    String name,
    String gstin
) {}
