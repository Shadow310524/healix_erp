package in.healix.modules.inventory.web.dto;

import in.healix.modules.inventory.domain.enums.TransferStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class StockTransferDTO {
    private UUID id;
    private UUID tenantId;
    private UUID sourceWarehouseId;
    private UUID targetWarehouseId;
    private UUID batchId;
    private int quantity;
    private TransferStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
