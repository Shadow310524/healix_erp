package in.healix.modules.inventory.web;

import in.healix.modules.inventory.domain.StockTransfer;
import in.healix.modules.inventory.mapper.InventoryMapper;
import in.healix.modules.inventory.service.StockTransferService;
import in.healix.modules.inventory.web.dto.StockTransferDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory/transfers")
public class StockTransferController {

    private final StockTransferService transferService;
    private final InventoryMapper inventoryMapper;

    public StockTransferController(StockTransferService transferService, InventoryMapper inventoryMapper) {
        this.transferService = transferService;
        this.inventoryMapper = inventoryMapper;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<StockTransferDTO> initiateTransfer(@RequestBody StockTransferDTO transferDTO) {
        StockTransfer transfer = inventoryMapper.toEntity(transferDTO);
        StockTransfer initiated = transferService.initiateTransfer(transfer);
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryMapper.toDto(initiated));
    }

    @PostMapping("/{id}/ship")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<StockTransferDTO> shipTransfer(@PathVariable("id") UUID id) {
        StockTransfer shipped = transferService.shipTransfer(id);
        return ResponseEntity.ok(inventoryMapper.toDto(shipped));
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<StockTransferDTO> receiveTransfer(@PathVariable("id") UUID id) {
        StockTransfer received = transferService.receiveTransfer(id);
        return ResponseEntity.ok(inventoryMapper.toDto(received));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<StockTransferDTO> cancelTransfer(@PathVariable("id") UUID id) {
        StockTransfer cancelled = transferService.cancelTransfer(id);
        return ResponseEntity.ok(inventoryMapper.toDto(cancelled));
    }
}
