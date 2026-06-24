package in.healix.modules.inventory.service;

import in.healix.modules.inventory.domain.InventoryBatch;
import in.healix.modules.inventory.domain.StockLedger;
import in.healix.modules.inventory.domain.StockTransfer;
import in.healix.modules.inventory.domain.enums.BatchStatus;
import in.healix.modules.inventory.domain.enums.StockTransactionType;
import in.healix.modules.inventory.domain.enums.TransferStatus;
import in.healix.modules.inventory.repository.InventoryBatchRepository;
import in.healix.modules.inventory.repository.StockLedgerRepository;
import in.healix.modules.inventory.repository.StockTransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class StockTransferService {

    private final StockTransferRepository transferRepository;
    private final InventoryBatchRepository batchRepository;
    private final StockLedgerRepository ledgerRepository;

    public StockTransferService(StockTransferRepository transferRepository,
                                InventoryBatchRepository batchRepository,
                                StockLedgerRepository ledgerRepository) {
        this.transferRepository = transferRepository;
        this.batchRepository = batchRepository;
        this.ledgerRepository = ledgerRepository;
    }

    public StockTransfer initiateTransfer(StockTransfer transfer) {
        InventoryBatch sourceBatch = batchRepository.findById(transfer.getBatch().getId())
                .orElseThrow(() -> new IllegalArgumentException("Source inventory batch not found"));

        if (!sourceBatch.getWarehouseId().equals(transfer.getSourceWarehouseId())) {
            throw new IllegalArgumentException("Source batch does not belong to the source warehouse");
        }

        int availableQty = sourceBatch.getQuantity() - sourceBatch.getBlockedQuantity();
        if (transfer.getQuantity() > availableQty) {
            throw new IllegalArgumentException("Insufficient available quantity in source batch for transfer");
        }

        // Block quantity on source batch
        sourceBatch.setBlockedQuantity(sourceBatch.getBlockedQuantity() + transfer.getQuantity());
        batchRepository.save(sourceBatch);

        transfer.setBatch(sourceBatch);
        transfer.setStatus(TransferStatus.REQUESTED);
        return transferRepository.save(transfer);
    }

    public StockTransfer shipTransfer(UUID transferId) {
        StockTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Stock transfer request not found"));

        if (transfer.getStatus() != TransferStatus.REQUESTED) {
            throw new IllegalStateException("Only REQUESTED transfers can be shipped");
        }

        transfer.setStatus(TransferStatus.SHIPPED);
        return transferRepository.save(transfer);
    }

    public StockTransfer receiveTransfer(UUID transferId) {
        StockTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Stock transfer request not found"));

        if (transfer.getStatus() != TransferStatus.SHIPPED) {
            throw new IllegalStateException("Only SHIPPED transfers can be received");
        }

        InventoryBatch sourceBatch = transfer.getBatch();

        // 1. Deduct quantity and blocked quantity from source batch
        sourceBatch.setQuantity(sourceBatch.getQuantity() - transfer.getQuantity());
        sourceBatch.setBlockedQuantity(sourceBatch.getBlockedQuantity() - transfer.getQuantity());
        if (sourceBatch.getQuantity() == 0) {
            sourceBatch.setStatus(BatchStatus.DESTRUCTED);
        }
        batchRepository.save(sourceBatch);

        // Record source ledger deduction
        StockLedger sourceLedger = new StockLedger();
        sourceLedger.setBatch(sourceBatch);
        sourceLedger.setTransactionType(StockTransactionType.TRANSFER);
        sourceLedger.setQuantity(-transfer.getQuantity());
        sourceLedger.setReferenceId(transfer.getId());
        sourceLedger.setNotes("Transfer out to warehouse: " + transfer.getTargetWarehouseId());
        ledgerRepository.save(sourceLedger);

        // 2. Add quantity to target warehouse batch
        Optional<InventoryBatch> targetBatchOpt = batchRepository.findByWarehouseIdAndProductIdAndBatchNo(
                transfer.getTargetWarehouseId(), sourceBatch.getProductId(), sourceBatch.getBatchNo());

        InventoryBatch targetBatch;
        if (targetBatchOpt.isPresent()) {
            targetBatch = targetBatchOpt.get();
            targetBatch.setQuantity(targetBatch.getQuantity() + transfer.getQuantity());
            if (targetBatch.getStatus() == BatchStatus.DESTRUCTED) {
                targetBatch.setStatus(BatchStatus.ACTIVE);
            }
            batchRepository.save(targetBatch);
        } else {
            targetBatch = new InventoryBatch();
            targetBatch.setProductId(sourceBatch.getProductId());
            targetBatch.setWarehouseId(transfer.getTargetWarehouseId());
            targetBatch.setBatchNo(sourceBatch.getBatchNo());
            targetBatch.setMfgDate(sourceBatch.getMfgDate());
            targetBatch.setExpiryDate(sourceBatch.getExpiryDate());
            targetBatch.setPurchaseRate(sourceBatch.getPurchaseRate());
            targetBatch.setMrp(sourceBatch.getMrp());
            targetBatch.setQuantity(transfer.getQuantity());
            targetBatch.setBlockedQuantity(0);
            targetBatch.setStatus(BatchStatus.ACTIVE);
            batchRepository.save(targetBatch);
        }

        // Record target ledger addition
        StockLedger targetLedger = new StockLedger();
        targetLedger.setBatch(targetBatch);
        targetLedger.setTransactionType(StockTransactionType.TRANSFER);
        targetLedger.setQuantity(transfer.getQuantity());
        targetLedger.setReferenceId(transfer.getId());
        targetLedger.setNotes("Transfer in from warehouse: " + transfer.getSourceWarehouseId());
        ledgerRepository.save(targetLedger);

        transfer.setStatus(TransferStatus.RECEIVED);
        return transferRepository.save(transfer);
    }

    public StockTransfer cancelTransfer(UUID transferId) {
        StockTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Stock transfer request not found"));

        if (transfer.getStatus() != TransferStatus.REQUESTED && transfer.getStatus() != TransferStatus.SHIPPED) {
            throw new IllegalStateException("Only REQUESTED or SHIPPED transfers can be cancelled");
        }

        InventoryBatch sourceBatch = transfer.getBatch();
        // Unblock quantity on source batch
        sourceBatch.setBlockedQuantity(sourceBatch.getBlockedQuantity() - transfer.getQuantity());
        batchRepository.save(sourceBatch);

        transfer.setStatus(TransferStatus.CANCELLED);
        return transferRepository.save(transfer);
    }
}
