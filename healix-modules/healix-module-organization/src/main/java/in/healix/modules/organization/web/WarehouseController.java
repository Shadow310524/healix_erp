package in.healix.modules.organization.web;

import in.healix.modules.organization.domain.Warehouse;
import in.healix.modules.organization.mapper.WarehouseMapper;
import in.healix.modules.organization.service.WarehouseService;
import in.healix.modules.organization.web.dto.WarehouseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final WarehouseMapper warehouseMapper;

    public WarehouseController(WarehouseService warehouseService, WarehouseMapper warehouseMapper) {
        this.warehouseService = warehouseService;
        this.warehouseMapper = warehouseMapper;
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<List<WarehouseDTO>> getWarehousesByBranch(@PathVariable UUID branchId) {
        List<WarehouseDTO> list = warehouseService.getWarehousesByBranch(branchId).stream()
                .map(warehouseMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<WarehouseDTO> getWarehouseById(@PathVariable UUID id) {
        Warehouse warehouse = warehouseService.getWarehouseById(id);
        return ResponseEntity.ok(warehouseMapper.toDto(warehouse));
    }

    @PostMapping("/branch/{branchId}")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<WarehouseDTO> createWarehouse(@PathVariable UUID branchId, @RequestBody WarehouseDTO warehouseDTO) {
        Warehouse warehouse = warehouseMapper.toEntity(warehouseDTO);
        Warehouse savedWarehouse = warehouseService.createWarehouse(branchId, warehouse);
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseMapper.toDto(savedWarehouse));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<WarehouseDTO> updateWarehouse(@PathVariable UUID id, @RequestBody WarehouseDTO warehouseDTO) {
        Warehouse warehouse = warehouseMapper.toEntity(warehouseDTO);
        Warehouse updatedWarehouse = warehouseService.updateWarehouse(id, warehouse);
        return ResponseEntity.ok(warehouseMapper.toDto(updatedWarehouse));
    }
}
