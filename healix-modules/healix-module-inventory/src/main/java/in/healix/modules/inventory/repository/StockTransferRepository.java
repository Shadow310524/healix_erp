package in.healix.modules.inventory.repository;

import in.healix.modules.inventory.domain.StockTransfer;
import in.healix.modules.inventory.domain.enums.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, UUID> {
    List<StockTransfer> findBySourceWarehouseId(UUID sourceWarehouseId);
    List<StockTransfer> findByTargetWarehouseId(UUID targetWarehouseId);
    List<StockTransfer> findByStatus(TransferStatus status);
}
