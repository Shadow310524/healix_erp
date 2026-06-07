package in.healix.modules.organization.web;

import in.healix.modules.organization.domain.Tenant;
import in.healix.modules.organization.mapper.TenantMapper;
import in.healix.modules.organization.service.TenantService;
import in.healix.modules.organization.web.dto.TenantDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TenantService tenantService;
    private final TenantMapper tenantMapper;

    public TenantController(TenantService tenantService, TenantMapper tenantMapper) {
        this.tenantService = tenantService;
        this.tenantMapper = tenantMapper;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<List<TenantDTO>> getAllTenants() {
        List<TenantDTO> list = tenantService.getAllTenants().stream()
                .map(tenantMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN') or hasAuthority('ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<TenantDTO> getTenantById(@PathVariable UUID id) {
        Tenant tenant = tenantService.getTenantById(id);
        return ResponseEntity.ok(tenantMapper.toDto(tenant));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<TenantDTO> updateTenantStatus(@PathVariable UUID id, @RequestParam String status) {
        Tenant tenant = tenantService.updateTenantStatus(id, status);
        return ResponseEntity.ok(tenantMapper.toDto(tenant));
    }
}
