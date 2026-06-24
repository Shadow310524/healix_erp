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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryBatchRepository batchRepository;

    @Mock
    private StockLedgerRepository ledgerRepository;

    @Mock
    private StockAdjustmentRepository adjustmentRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void createBatch_WithValidData_ShouldSaveBatchAndLogLedger() {
        // Arrange
        InventoryBatch batch = new InventoryBatch();
        batch.setProductId(UUID.randomUUID());
        batch.setWarehouseId(UUID.randomUUID());
        batch.setBatchNo("B123");
        batch.setMfgDate(LocalDate.now().minusMonths(1));
        batch.setExpiryDate(LocalDate.now().plusYears(1));
        batch.setPurchaseRate(BigDecimal.valueOf(10.00));
        batch.setMrp(BigDecimal.valueOf(15.00));
        batch.setQuantity(100);

        when(batchRepository.findByWarehouseIdAndProductIdAndBatchNo(any(), any(), any())).thenReturn(Optional.empty());
        when(batchRepository.save(any(InventoryBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        InventoryBatch saved = inventoryService.createBatch(batch);

        // Assert
        assertNotNull(saved);
        assertEquals("B123", saved.getBatchNo());
        verify(batchRepository, times(1)).save(batch);
        verify(ledgerRepository, times(1)).save(any(StockLedger.class));
    }

    @Test
    void createBatch_WithPastExpiry_ShouldThrowException() {
        // Arrange
        InventoryBatch batch = new InventoryBatch();
        batch.setProductId(UUID.randomUUID());
        batch.setWarehouseId(UUID.randomUUID());
        batch.setBatchNo("B123");
        batch.setMfgDate(LocalDate.now().minusMonths(6));
        batch.setExpiryDate(LocalDate.now().minusDays(1)); // Past expiry
        batch.setPurchaseRate(BigDecimal.valueOf(10.00));
        batch.setMrp(BigDecimal.valueOf(15.00));
        batch.setQuantity(100);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> inventoryService.createBatch(batch));
        assertEquals("Expiry date must be in the future for new batches", ex.getMessage());
        verify(batchRepository, never()).save(any());
    }

    @Test
    void createBatch_WithMrpLessThanPurchaseRate_ShouldThrowException() {
        // Arrange
        InventoryBatch batch = new InventoryBatch();
        batch.setProductId(UUID.randomUUID());
        batch.setWarehouseId(UUID.randomUUID());
        batch.setBatchNo("B123");
        batch.setMfgDate(LocalDate.now().minusMonths(1));
        batch.setExpiryDate(LocalDate.now().plusYears(1));
        batch.setPurchaseRate(BigDecimal.valueOf(20.00));
        batch.setMrp(BigDecimal.valueOf(15.00)); // MRP < PurchaseRate (Invalid)
        batch.setQuantity(100);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> inventoryService.createBatch(batch));
        assertEquals("MRP cannot be less than the Purchase Rate", ex.getMessage());
        verify(batchRepository, never()).save(any());
    }

    @Test
    void createBatch_ExistingBatch_ShouldMergeQuantities() {
        // Arrange
        InventoryBatch existing = new InventoryBatch();
        existing.setId(UUID.randomUUID());
        existing.setProductId(UUID.randomUUID());
        existing.setWarehouseId(UUID.randomUUID());
        existing.setBatchNo("B123");
        existing.setMfgDate(LocalDate.now().minusMonths(1));
        existing.setExpiryDate(LocalDate.now().plusYears(1));
        existing.setPurchaseRate(BigDecimal.valueOf(10.00));
        existing.setMrp(BigDecimal.valueOf(15.00));
        existing.setQuantity(100);

        InventoryBatch incoming = new InventoryBatch();
        incoming.setProductId(existing.getProductId());
        incoming.setWarehouseId(existing.getWarehouseId());
        incoming.setBatchNo("B123");
        incoming.setMfgDate(existing.getMfgDate());
        incoming.setExpiryDate(existing.getExpiryDate());
        incoming.setPurchaseRate(existing.getPurchaseRate());
        incoming.setMrp(existing.getMrp());
        incoming.setQuantity(50); // incoming quantity = 50

        when(batchRepository.findByWarehouseIdAndProductIdAndBatchNo(any(), any(), any())).thenReturn(Optional.of(existing));
        when(batchRepository.save(any(InventoryBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        InventoryBatch saved = inventoryService.createBatch(incoming);

        // Assert
        assertNotNull(saved);
        assertEquals(150, saved.getQuantity()); // 100 + 50
        verify(batchRepository, times(1)).save(existing);
        verify(ledgerRepository, times(1)).save(any(StockLedger.class));
    }

    @Test
    void adjustStock_ValidAdjustment_ShouldUpdateQuantityAndLogLedger() {
        // Arrange
        UUID batchId = UUID.randomUUID();
        InventoryBatch batch = new InventoryBatch();
        batch.setId(batchId);
        batch.setQuantity(100);

        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
        when(batchRepository.save(any(InventoryBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(adjustmentRepository.save(any(StockAdjustment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        StockAdjustment adjustment = inventoryService.adjustStock(
                batchId, 80, AdjustmentReason.DAMAGED, UUID.randomUUID());

        // Assert
        assertNotNull(adjustment);
        assertEquals(100, adjustment.getQuantityBefore());
        assertEquals(80, adjustment.getQuantityAfter());
        assertEquals(80, batch.getQuantity());
        verify(batchRepository, times(1)).save(batch);
        verify(adjustmentRepository, times(1)).save(any(StockAdjustment.class));
        verify(ledgerRepository, times(1)).save(any(StockLedger.class));
    }

    @Test
    void adjustStock_NegativeQuantity_ShouldThrowException() {
        // Arrange
        UUID batchId = UUID.randomUUID();
        InventoryBatch batch = new InventoryBatch();
        batch.setId(batchId);
        batch.setQuantity(100);

        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                inventoryService.adjustStock(batchId, -5, AdjustmentReason.THEFT, UUID.randomUUID()));
        assertEquals("Adjusted stock quantity cannot be negative", ex.getMessage());
        verify(batchRepository, never()).save(any());
    }

    @Test
    void getFefoBatches_ShouldReturnBatchesSortedByExpiryAsc() {
        // Arrange
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        InventoryBatch batch1 = new InventoryBatch();
        batch1.setExpiryDate(LocalDate.now().plusMonths(6));
        InventoryBatch batch2 = new InventoryBatch();
        batch2.setExpiryDate(LocalDate.now().plusMonths(3)); // Expires earlier

        when(batchRepository.findByProductIdAndWarehouseIdAndStatusAndQuantityGreaterThanOrderByExpiryDateAsc(
                productId, warehouseId, BatchStatus.ACTIVE, 0)).thenReturn(List.of(batch2, batch1));

        // Act
        List<InventoryBatch> results = inventoryService.getFefoBatches(productId, warehouseId);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(batch2, results.get(0)); // FEFO: earliest expiry first
    }
}
