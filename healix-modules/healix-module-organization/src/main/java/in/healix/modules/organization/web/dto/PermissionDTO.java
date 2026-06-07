package in.healix.modules.organization.web.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class PermissionDTO {
    private UUID id;
    private UUID tenantId;
    private String resource;
    private String action;
    private String description;
    private Integer version;
}
