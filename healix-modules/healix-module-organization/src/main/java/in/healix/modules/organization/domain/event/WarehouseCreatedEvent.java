package in.healix.modules.organization.domain.event;

import java.util.UUID;

public record WarehouseCreatedEvent(
    UUID eventId,
    UUID tenantId,
    UUID warehouseId,
    UUID branchId,
    String name
) {}
