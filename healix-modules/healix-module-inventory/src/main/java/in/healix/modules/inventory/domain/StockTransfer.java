package in.healix.modules.inventory.domain;

import in.healix.modules.inventory.domain.enums.TransferStatus;
import in.healix.persistence.domain.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "stock_transfers")
@Getter
@Setter
public class StockTransfer extends TenantAwareEntity {

    @Column(name = "source_warehouse_id", nullable = false)
    private UUID sourceWarehouseId;

    @Column(name = "target_warehouse_id", nullable = false)
    private UUID targetWarehouseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private InventoryBatch batch;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status = TransferStatus.REQUESTED;
}
