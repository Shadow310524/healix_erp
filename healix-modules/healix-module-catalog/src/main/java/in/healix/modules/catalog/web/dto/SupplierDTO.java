package in.healix.modules.catalog.web.dto;

import in.healix.modules.catalog.domain.enums.PaymentMode;
import in.healix.modules.catalog.domain.enums.SupplierStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class SupplierDTO {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String gstin;
    private String pan;
    private String drugLicenseNo;
    private String address;
    private String contactPhone;
    private String contactEmail;
    private int creditDays;
    private BigDecimal creditLimit;
    private PaymentMode paymentMode;
    private SupplierStatus status;
    private Integer version;
}
