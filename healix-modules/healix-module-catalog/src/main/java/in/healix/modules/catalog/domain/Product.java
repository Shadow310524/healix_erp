package in.healix.modules.catalog.domain;

import in.healix.modules.catalog.domain.enums.DrugSchedule;
import in.healix.modules.catalog.domain.enums.ProductStatus;
import in.healix.persistence.domain.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product extends TenantAwareEntity {

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @Column(name = "generic_name", nullable = false)
    private String genericName;

    @Column(nullable = false)
    private String manufacturer;

    @Column(name = "hsn_code", nullable = false)
    private String hsnCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DrugSchedule schedule = DrugSchedule.OTC;

    @Column(name = "dosage_form", nullable = false)
    private String dosageForm = "TABLET";

    @Column(nullable = false)
    private String strength;

    @Column(name = "pack_size", nullable = false)
    private String packSize;

    @Column(nullable = false)
    private BigDecimal mrp;

    @Column(nullable = false)
    private BigDecimal ptr;

    @Column(name = "purchase_rate", nullable = false)
    private BigDecimal purchaseRate;

    @Column(name = "gst_rate", nullable = false)
    private BigDecimal gstRate = BigDecimal.valueOf(12.00);

    @Column(name = "is_narcotic", nullable = false)
    private boolean isNarcotic = false;

    @Column(name = "is_cold_chain", nullable = false)
    private boolean isColdChain = false;

    @Column(name = "rack_location")
    private String rackLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_suppliers",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "supplier_id")
    )
    private Set<Supplier> approvedSuppliers = new HashSet<>();
}
