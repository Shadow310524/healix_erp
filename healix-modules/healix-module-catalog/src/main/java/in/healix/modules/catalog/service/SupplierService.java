package in.healix.modules.catalog.service;

import in.healix.core.exception.HealixException;
import in.healix.core.tenant.TenantContext;
import in.healix.modules.catalog.domain.Supplier;
import in.healix.modules.catalog.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public Supplier getSupplierById(UUID id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new HealixException("Supplier not found", "SUPPLIER_NOT_FOUND"));
    }

    @Transactional
    public Supplier createSupplier(Supplier supplier) {
        String tenantIdStr = TenantContext.getTenantId();
        if (tenantIdStr == null) {
            throw new HealixException("Tenant context required to create supplier", "TENANT_CONTEXT_MISSING");
        }
        supplier.setTenantId(UUID.fromString(tenantIdStr));

        // Name uniqueness check within tenant
        if (supplierRepository.findByName(supplier.getName()).isPresent()) {
            throw new HealixException("Supplier with same name already exists", "DUPLICATE_SUPPLIER_NAME");
        }

        validateSupplier(supplier);

        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier updateSupplier(UUID id, Supplier updatedSupplier) {
        Supplier supplier = getSupplierById(id);

        if (!supplier.getName().equalsIgnoreCase(updatedSupplier.getName()) &&
                supplierRepository.findByName(updatedSupplier.getName()).isPresent()) {
            throw new HealixException("Supplier with same name already exists", "DUPLICATE_SUPPLIER_NAME");
        }

        supplier.setName(updatedSupplier.getName());
        supplier.setGstin(updatedSupplier.getGstin());
        supplier.setPan(updatedSupplier.getPan());
        supplier.setDrugLicenseNo(updatedSupplier.getDrugLicenseNo());
        supplier.setAddress(updatedSupplier.getAddress());
        supplier.setContactPhone(updatedSupplier.getContactPhone());
        supplier.setContactEmail(updatedSupplier.getContactEmail());
        supplier.setCreditDays(updatedSupplier.getCreditDays());
        supplier.setCreditLimit(updatedSupplier.getCreditLimit());
        supplier.setPaymentMode(updatedSupplier.getPaymentMode());
        supplier.setStatus(updatedSupplier.getStatus());

        validateSupplier(supplier);

        return supplierRepository.save(supplier);
    }

    private void validateSupplier(Supplier supplier) {
        if (supplier.getGstin() != null && !supplier.getGstin().trim().isEmpty()) {
            if (supplier.getGstin().length() != 15) {
                throw new HealixException("GSTIN must be exactly 15 characters", "INVALID_GSTIN");
            }
        }
        if (supplier.getCreditDays() < 0) {
            throw new HealixException("Credit days cannot be negative", "INVALID_CREDIT_DAYS");
        }
        if (supplier.getCreditLimit() != null && supplier.getCreditLimit().compareTo(BigDecimal.ZERO) < 0) {
            throw new HealixException("Credit limit cannot be negative", "INVALID_CREDIT_LIMIT");
        }
    }
}
