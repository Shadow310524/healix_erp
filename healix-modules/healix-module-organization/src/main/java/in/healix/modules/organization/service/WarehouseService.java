package in.healix.modules.organization.service;

import in.healix.core.exception.HealixException;
import in.healix.core.tenant.TenantContext;
import in.healix.modules.organization.domain.Branch;
import in.healix.modules.organization.domain.Warehouse;
import in.healix.modules.organization.domain.event.WarehouseCreatedEvent;
import in.healix.modules.organization.repository.WarehouseRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final BranchService branchService;
    private final ApplicationEventPublisher eventPublisher;

    public WarehouseService(WarehouseRepository warehouseRepository, BranchService branchService,
                            ApplicationEventPublisher eventPublisher) {
        this.warehouseRepository = warehouseRepository;
        this.branchService = branchService;
        this.eventPublisher = eventPublisher;
    }

    public List<Warehouse> getWarehousesByBranch(UUID branchId) {
        return warehouseRepository.findByBranchId(branchId);
    }

    public Warehouse getWarehouseById(UUID id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new HealixException("Warehouse not found", "WAREHOUSE_NOT_FOUND"));
    }

    @Transactional
    public Warehouse createWarehouse(UUID branchId, Warehouse warehouse) {
        Branch branch = branchService.getBranchById(branchId);
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new HealixException("Tenant context required to create warehouse", "TENANT_CONTEXT_MISSING");
        }
        warehouse.setBranch(branch);
        warehouse.setTenantId(UUID.fromString(tenantId));
        Warehouse savedWarehouse = warehouseRepository.save(warehouse);

        // Publish domain event
        eventPublisher.publishEvent(new WarehouseCreatedEvent(
            UUID.randomUUID(),
            savedWarehouse.getTenantId(),
            savedWarehouse.getId(),
            branch.getId(),
            savedWarehouse.getName()
        ));

        return savedWarehouse;
    }

    @Transactional
    public Warehouse updateWarehouse(UUID id, Warehouse updatedWarehouse) {
        Warehouse warehouse = getWarehouseById(id);
        warehouse.setName(updatedWarehouse.getName());
        warehouse.setType(updatedWarehouse.getType());
        warehouse.setStatus(updatedWarehouse.getStatus());
        return warehouseRepository.save(warehouse);
    }
}
