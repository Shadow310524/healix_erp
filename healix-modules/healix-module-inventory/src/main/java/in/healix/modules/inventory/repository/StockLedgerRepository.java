package in.healix.modules.inventory.repository;

import in.healix.modules.inventory.domain.StockLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StockLedgerRepository extends JpaRepository<StockLedger, UUID> {
    List<StockLedger> findByBatchId(UUID batchId);
}
