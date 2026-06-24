package in.healix.modules.inventory.web.dto;

import in.healix.modules.inventory.domain.enums.BatchStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class InventoryBatchDTO {
    private UUID id;
    private UUID tenantId;
    private UUID productId;
    private UUID warehouseId;
    private String batchNo;
    private LocalDate mfgDate;
    private LocalDate expiryDate;
    private BigDecimal purchaseRate;
    private BigDecimal mrp;
    private int quantity;
    private int blockedQuantity;
    private BatchStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
