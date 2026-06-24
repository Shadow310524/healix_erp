package in.healix.modules.inventory.service;

import in.healix.modules.inventory.domain.InventoryBatch;
import in.healix.modules.inventory.domain.StockAdjustment;
import in.healix.modules.inventory.domain.StockLedger;
import in.healix.modules.inventory.domain.enums.AdjustmentReason;
import in.healix.modules.inventory.domain.enums.BatchStatus;
import in.healix.modules.inventory.domain.enums.StockTransactionType;
import in.healix.modules.inventory.repository.InventoryBatchRepository;
import in.healix.modules.inventory.repository.StockAdjustmentRepository;
import in.healix.modules.inventory.repository.StockLedgerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class InventoryService {

    private final InventoryBatchRepository batchRepository;
    private final StockLedgerRepository ledgerRepository;
    private final StockAdjustmentRepository adjustmentRepository;

    public InventoryService(InventoryBatchRepository batchRepository,
                            StockLedgerRepository ledgerRepository,
                            StockAdjustmentRepository adjustmentRepository) {
        this.batchRepository = batchRepository;
        this.ledgerRepository = ledgerRepository;
        this.adjustmentRepository = adjustmentRepository;
    }

    public InventoryBatch createBatch(InventoryBatch batch) {
        if (batch.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Expiry date must be in the future for new batches");
        }
        if (batch.getMrp().compareTo(batch.getPurchaseRate()) < 0) {
            throw new IllegalArgumentException("MRP cannot be less than the Purchase Rate");
        }

        Optional<InventoryBatch> existingOpt = batchRepository.findByWarehouseIdAndProductIdAndBatchNo(
                batch.getWarehouseId(), batch.getProductId(), batch.getBatchNo());

        InventoryBatch savedBatch;
        int transactionQuantity = batch.getQuantity();

        if (existingOpt.isPresent()) {
            InventoryBatch existing = existingOpt.get();
            existing.setQuantity(existing.getQuantity() + batch.getQuantity());
            savedBatch = batchRepository.save(existing);
        } else {
            savedBatch = batchRepository.save(batch);
        }

        // Post ledger audit record
        StockLedger ledger = new StockLedger();
        ledger.setBatch(savedBatch);
        ledger.setTransactionType(StockTransactionType.PURCHASE);
        ledger.setQuantity(transactionQuantity);
        ledger.setReferenceId(savedBatch.getId());
        ledger.setNotes("Batch initial supply / replenishment");
        ledgerRepository.save(ledger);

        return savedBatch;
    }

    public StockAdjustment adjustStock(UUID batchId, int quantityAfter, AdjustmentReason reason, UUID approvedBy) {
        InventoryBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory batch not found with id: " + batchId));

        if (quantityAfter < 0) {
            throw new IllegalArgumentException("Adjusted stock quantity cannot be negative");
        }

        int quantityBefore = batch.getQuantity();
        int difference = quantityAfter - quantityBefore;

        if (difference == 0) {
            throw new IllegalArgumentException("Adjustment quantity after is same as current quantity");
        }

        // Update batch quantity
        batch.setQuantity(quantityAfter);
        if (quantityAfter == 0) {
            batch.setStatus(BatchStatus.DESTRUCTED);
        }
        batchRepository.save(batch);

        // Record adjustment log
        StockAdjustment adjustment = new StockAdjustment();
        adjustment.setBatch(batch);
        adjustment.setQuantityBefore(quantityBefore);
        adjustment.setQuantityAfter(quantityAfter);
        adjustment.setReason(reason);
        adjustment.setApprovedBy(approvedBy);
        StockAdjustment savedAdjustment = adjustmentRepository.save(adjustment);

        // Record ledger entry
        StockLedger ledger = new StockLedger();
        ledger.setBatch(batch);
        ledger.setTransactionType(StockTransactionType.ADJUSTMENT);
        ledger.setQuantity(difference);
        ledger.setReferenceId(savedAdjustment.getId());
        ledger.setNotes("Manual stock adjustment due to: " + reason);
        ledgerRepository.save(ledger);

        return savedAdjustment;
    }

    @Transactional(readOnly = true)
    public List<InventoryBatch> getFefoBatches(UUID productId, UUID warehouseId) {
        return batchRepository.findByProductIdAndWarehouseIdAndStatusAndQuantityGreaterThanOrderByExpiryDateAsc(
                productId, warehouseId, BatchStatus.ACTIVE, 0);
    }

    @Transactional(readOnly = true)
    public List<InventoryBatch> getExpiringBatches(int thresholdDays) {
        LocalDate dateLimit = LocalDate.now().plusDays(thresholdDays);
        return batchRepository.findByExpiryDateLessThanEqualAndStatus(dateLimit, BatchStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public int getAvailableStock(UUID productId, UUID warehouseId) {
        return batchRepository.getAvailableQuantity(productId, warehouseId, BatchStatus.ACTIVE);
    }
}
