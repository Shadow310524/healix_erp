package in.healix.modules.catalog.domain;

import in.healix.modules.catalog.domain.enums.PaymentMode;
import in.healix.modules.catalog.domain.enums.SupplierStatus;
import in.healix.persistence.domain.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
public class Supplier extends TenantAwareEntity {

    @Column(nullable = false)
    private String name;

    private String gstin;

    private String pan;

    @Column(name = "drug_license_no")
    private String drugLicenseNo;

    private String address;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "credit_days", nullable = false)
    private int creditDays = 30;

    @Column(name = "credit_limit", nullable = false)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false)
    private PaymentMode paymentMode = PaymentMode.BANK_TRANSFER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupplierStatus status = SupplierStatus.ACTIVE;
}
