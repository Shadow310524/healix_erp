package in.healix.modules.organization.web.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class WarehouseDTO {
    private UUID id;
    private UUID tenantId;
    private UUID branchId;
    private String name;
    private String type;
    private String status;
    private Integer version;
}
