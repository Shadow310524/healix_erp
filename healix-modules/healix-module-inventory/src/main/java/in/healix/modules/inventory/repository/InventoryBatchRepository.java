package in.healix.modules.inventory.repository;

import in.healix.modules.inventory.domain.InventoryBatch;
import in.healix.modules.inventory.domain.enums.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, UUID> {

    Optional<InventoryBatch> findByWarehouseIdAndProductIdAndBatchNo(UUID warehouseId, UUID productId, String batchNo);

    // FEFO (First Expired, First Out) batch resolution
    List<InventoryBatch> findByProductIdAndWarehouseIdAndStatusAndQuantityGreaterThanOrderByExpiryDateAsc(
            UUID productId, UUID warehouseId, BatchStatus status, int minQuantity);

    // Query batches expiring on or before a given date
    List<InventoryBatch> findByExpiryDateLessThanEqualAndStatus(LocalDate date, BatchStatus status);

    // Compute total available stock for a product in a warehouse
    @Query("SELECT COALESCE(SUM(b.quantity - b.blockedQuantity), 0) FROM InventoryBatch b " +
           "WHERE b.productId = :productId AND b.warehouseId = :warehouseId AND b.status = :status")
    int getAvailableQuantity(@Param("productId") UUID productId, 
                             @Param("warehouseId") UUID warehouseId, 
                             @Param("status") BatchStatus status);
}
