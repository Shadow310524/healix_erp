package in.healix.modules.organization.web.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class BranchDTO {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String gstin;
    private String stateCode;
    private String address;
    private String status;
    private Integer version;
}
