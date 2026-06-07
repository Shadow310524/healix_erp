package in.healix.modules.organization.service;

import in.healix.core.exception.HealixException;
import in.healix.core.tenant.TenantContext;
import in.healix.modules.organization.domain.Permission;
import in.healix.modules.organization.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public Permission getPermissionById(UUID id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new HealixException("Permission not found", "PERMISSION_NOT_FOUND"));
    }

    @Transactional
    public Permission createPermission(Permission permission) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new HealixException("Tenant context required to create permission", "TENANT_CONTEXT_MISSING");
        }
        permission.setTenantId(UUID.fromString(tenantId));
        return permissionRepository.save(permission);
    }
}
