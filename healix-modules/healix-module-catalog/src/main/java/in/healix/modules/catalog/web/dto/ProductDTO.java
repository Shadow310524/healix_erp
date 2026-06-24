package in.healix.modules.catalog.web.dto;

import in.healix.modules.catalog.domain.enums.DrugSchedule;
import in.healix.modules.catalog.domain.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Data
public class ProductDTO {
    private UUID id;
    private UUID tenantId;
    private String brandName;
    private String genericName;
    private String manufacturer;
    private String hsnCode;
    private DrugSchedule schedule;
    private String dosageForm;
    private String strength;
    private String packSize;
    private BigDecimal mrp;
    private BigDecimal ptr;
    private BigDecimal purchaseRate;
    private BigDecimal gstRate;
    private boolean isNarcotic;
    private boolean isColdChain;
    private String rackLocation;
    private ProductStatus status;
    private Set<UUID> approvedSupplierIds;
    private Integer version;
}
