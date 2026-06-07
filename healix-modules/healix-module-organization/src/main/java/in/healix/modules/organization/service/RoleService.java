package in.healix.modules.organization.service;

import in.healix.core.exception.HealixException;
import in.healix.core.tenant.TenantContext;
import in.healix.modules.organization.domain.Permission;
import in.healix.modules.organization.domain.Role;
import in.healix.modules.organization.repository.PermissionRepository;
import in.healix.modules.organization.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleById(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new HealixException("Role not found", "ROLE_NOT_FOUND"));
    }

    @Transactional
    public Role createRole(Role role) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new HealixException("Tenant context required to create role", "TENANT_CONTEXT_MISSING");
        }
        role.setTenantId(UUID.fromString(tenantId));
        return roleRepository.save(role);
    }

    @Transactional
    public Role grantPermission(UUID roleId, UUID permissionId) {
        Role role = getRoleById(roleId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new HealixException("Permission not found", "PERMISSION_NOT_FOUND"));

        role.getPermissions().add(permission);
        return roleRepository.save(role);
    }
}
