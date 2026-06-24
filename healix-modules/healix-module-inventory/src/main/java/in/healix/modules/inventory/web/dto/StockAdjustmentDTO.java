package in.healix.modules.inventory.web.dto;

import in.healix.modules.inventory.domain.enums.AdjustmentReason;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class StockAdjustmentDTO {
    private UUID id;
    private UUID tenantId;
    private UUID batchId;
    private int quantityBefore;
    private int quantityAfter;
    private AdjustmentReason reason;
    private UUID approvedBy;
    private Instant createdAt;
}
