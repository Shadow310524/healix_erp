package in.healix.modules.inventory.web;

import in.healix.modules.inventory.domain.InventoryBatch;
import in.healix.modules.inventory.domain.StockAdjustment;
import in.healix.modules.inventory.domain.enums.AdjustmentReason;
import in.healix.modules.inventory.mapper.InventoryMapper;
import in.healix.modules.inventory.service.InventoryService;
import in.healix.modules.inventory.web.dto.InventoryBatchDTO;
import in.healix.modules.inventory.web.dto.StockAdjustmentDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryMapper inventoryMapper;

    public InventoryController(InventoryService inventoryService, InventoryMapper inventoryMapper) {
        this.inventoryService = inventoryService;
        this.inventoryMapper = inventoryMapper;
    }

    @PostMapping("/batches")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<InventoryBatchDTO> createBatch(@RequestBody InventoryBatchDTO batchDTO) {
        InventoryBatch batch = inventoryMapper.toEntity(batchDTO);
        InventoryBatch created = inventoryService.createBatch(batch);
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryMapper.toDto(created));
    }

    @PostMapping("/adjustments")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<StockAdjustmentDTO> adjustStock(
            @RequestParam("batchId") UUID batchId,
            @RequestParam("quantityAfter") int quantityAfter,
            @RequestParam("reason") AdjustmentReason reason,
            @RequestParam("approvedBy") UUID approvedBy) {
        StockAdjustment adjustment = inventoryService.adjustStock(batchId, quantityAfter, reason, approvedBy);
        return ResponseEntity.ok(inventoryMapper.toDto(adjustment));
    }

    @GetMapping("/batches/fefo")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<List<InventoryBatchDTO>> getFefoBatches(
            @RequestParam("productId") UUID productId,
            @RequestParam("warehouseId") UUID warehouseId) {
        List<InventoryBatch> batches = inventoryService.getFefoBatches(productId, warehouseId);
        List<InventoryBatchDTO> dtos = batches.stream()
                .map(inventoryMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/batches/expiring")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<List<InventoryBatchDTO>> getExpiringBatches(@RequestParam("days") int days) {
        List<InventoryBatch> batches = inventoryService.getExpiringBatches(days);
        List<InventoryBatchDTO> dtos = batches.stream()
                .map(inventoryMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/stock")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Integer> getAvailableStock(
            @RequestParam("productId") UUID productId,
            @RequestParam("warehouseId") UUID warehouseId) {
        int stock = inventoryService.getAvailableStock(productId, warehouseId);
        return ResponseEntity.ok(stock);
    }
}
