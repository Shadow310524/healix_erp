package in.healix.modules.organization.web;

import in.healix.modules.organization.domain.Role;
import in.healix.modules.organization.mapper.RoleMapper;
import in.healix.modules.organization.service.RoleService;
import in.healix.modules.organization.web.dto.RoleDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;
    private final RoleMapper roleMapper;

    public RoleController(RoleService roleService, RoleMapper roleMapper) {
        this.roleService = roleService;
        this.roleMapper = roleMapper;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<RoleDTO> list = roleService.getAllRoles().stream()
                .map(roleMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable UUID id) {
        Role role = roleService.getRoleById(id);
        return ResponseEntity.ok(roleMapper.toDto(role));
     }
 
     @PostMapping
     @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
     public ResponseEntity<RoleDTO> createRole(@RequestBody RoleDTO roleDTO) {
         Role role = roleMapper.toEntity(roleDTO);
         Role savedRole = roleService.createRole(role);
         return ResponseEntity.status(HttpStatus.CREATED).body(roleMapper.toDto(savedRole));
     }
 
     @PostMapping("/{id}/permissions/{permissionId}")
     @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
     public ResponseEntity<RoleDTO> grantPermission(@PathVariable UUID id, @PathVariable UUID permissionId) {
         Role role = roleService.grantPermission(id, permissionId);
         return ResponseEntity.ok(roleMapper.toDto(role));
     }
}
