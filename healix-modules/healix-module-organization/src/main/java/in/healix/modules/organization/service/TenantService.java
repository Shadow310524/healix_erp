package in.healix.modules.organization.service;

import in.healix.core.exception.HealixException;
import in.healix.modules.organization.domain.Tenant;
import in.healix.modules.organization.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Tenant getTenantById(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new HealixException("Tenant not found", "TENANT_NOT_FOUND"));
    }

    @Transactional
    public Tenant updateTenantStatus(UUID id, String status) {
        Tenant tenant = getTenantById(id);
        if (!status.equals("ACTIVE") && !status.equals("SUSPENDED") && !status.equals("DELETED")) {
            throw new HealixException("Invalid tenant status transition requested", "INVALID_STATUS");
        }
        tenant.setStatus(status);
        return tenantRepository.save(tenant);
    }
}
