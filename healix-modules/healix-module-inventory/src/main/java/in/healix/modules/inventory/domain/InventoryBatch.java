package in.healix.modules.inventory.domain;

import in.healix.modules.inventory.domain.enums.BatchStatus;
import in.healix.persistence.domain.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "inventory_batches")
@Getter
@Setter
public class InventoryBatch extends TenantAwareEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "batch_no", nullable = false)
    private String batchNo;

    @Column(name = "mfg_date", nullable = false)
    private LocalDate mfgDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "purchase_rate", nullable = false)
    private BigDecimal purchaseRate;

    @Column(nullable = false)
    private BigDecimal mrp;

    @Column(nullable = false)
    private int quantity = 0;

    @Column(name = "blocked_quantity", nullable = false)
    private int blockedQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchStatus status = BatchStatus.ACTIVE;
}
