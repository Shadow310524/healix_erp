package in.healix.modules.catalog.web;

import in.healix.modules.catalog.domain.Supplier;
import in.healix.modules.catalog.mapper.SupplierMapper;
import in.healix.modules.catalog.service.SupplierService;
import in.healix.modules.catalog.web.dto.SupplierDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierController {

    private final SupplierService supplierService;
    private final SupplierMapper supplierMapper;

    public SupplierController(SupplierService supplierService, SupplierMapper supplierMapper) {
        this.supplierService = supplierService;
        this.supplierMapper = supplierMapper;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<List<SupplierDTO>> getAllSuppliers() {
        List<SupplierDTO> list = supplierService.getAllSuppliers().stream()
                .map(supplierMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<SupplierDTO> getSupplierById(@PathVariable UUID id) {
        Supplier supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(supplierMapper.toDto(supplier));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<SupplierDTO> createSupplier(@RequestBody SupplierDTO supplierDTO) {
        Supplier supplier = supplierMapper.toEntity(supplierDTO);
        Supplier savedSupplier = supplierService.createSupplier(supplier);
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierMapper.toDto(savedSupplier));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<SupplierDTO> updateSupplier(@PathVariable UUID id, @RequestBody SupplierDTO supplierDTO) {
        Supplier supplier = supplierMapper.toEntity(supplierDTO);
        Supplier updatedSupplier = supplierService.updateSupplier(id, supplier);
        return ResponseEntity.ok(supplierMapper.toDto(updatedSupplier));
    }
}
