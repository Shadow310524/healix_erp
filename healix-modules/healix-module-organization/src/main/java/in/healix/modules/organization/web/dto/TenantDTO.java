package in.healix.modules.organization.web.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class TenantDTO {
    private UUID id;
    private String name;
    private String subdomain;
    private String pan;
    private String status;
    private Integer version;
}
