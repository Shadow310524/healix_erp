package in.healix.modules.organization.web.dto;

import lombok.Data;
import java.util.UUID;
import java.util.Set;

@Data
public class RoleDTO {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String description;
    private Set<UUID> permissionIds;
    private Integer version;
}
