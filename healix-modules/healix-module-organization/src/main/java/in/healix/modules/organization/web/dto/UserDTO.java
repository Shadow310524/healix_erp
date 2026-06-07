package in.healix.modules.organization.web.dto;

import lombok.Data;
import java.util.UUID;
import java.util.Set;

@Data
public class UserDTO {
    private UUID id;
    private UUID tenantId;
    private String email;
    private String firstName;
    private String lastName;
    private String status;
    private Set<UUID> roleIds;
    private Integer version;
}
