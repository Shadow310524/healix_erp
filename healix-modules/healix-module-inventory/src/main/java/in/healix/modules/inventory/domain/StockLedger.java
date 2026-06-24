package in.healix.modules.inventory.domain;

import in.healix.modules.inventory.domain.enums.StockTransactionType;
import in.healix.persistence.domain.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "stock_ledgers")
@Getter
@Setter
public class StockLedger extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private InventoryBatch batch;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private StockTransactionType transactionType;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;

    @Column
    private String notes;
}
