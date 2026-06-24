package in.healix.modules.catalog.service;

import in.healix.core.exception.HealixException;
import in.healix.core.tenant.TenantContext;
import in.healix.modules.catalog.domain.Product;
import in.healix.modules.catalog.domain.Supplier;
import in.healix.modules.catalog.repository.ProductRepository;
import in.healix.modules.catalog.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;

    public ProductService(ProductRepository productRepository, SupplierRepository supplierRepository) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new HealixException("Product not found", "PRODUCT_NOT_FOUND"));
    }

    @Transactional
    public Product createProduct(Product product, Set<UUID> approvedSupplierIds) {
        String tenantIdStr = TenantContext.getTenantId();
        if (tenantIdStr == null) {
            throw new HealixException("Tenant context required to create product", "TENANT_CONTEXT_MISSING");
        }
        UUID tenantId = UUID.fromString(tenantIdStr);
        product.setTenantId(tenantId);

        // Uniqueness check
        if (productRepository.findByBrandNameAndGenericNameAndStrength(
                product.getBrandName(), product.getGenericName(), product.getStrength()).isPresent()) {
            throw new HealixException("Product with same brand name, generic name, and strength already exists", "DUPLICATE_PRODUCT");
        }

        validateProduct(product);
        resolveAndBindSuppliers(product, approvedSupplierIds, tenantId);

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(UUID id, Product updatedProduct, Set<UUID> approvedSupplierIds) {
        Product product = getProductById(id);
        UUID tenantId = product.getTenantId();

        boolean identityChanged = !product.getBrandName().equalsIgnoreCase(updatedProduct.getBrandName()) ||
                !product.getGenericName().equalsIgnoreCase(updatedProduct.getGenericName()) ||
                !product.getStrength().equalsIgnoreCase(updatedProduct.getStrength());

        if (identityChanged && productRepository.findByBrandNameAndGenericNameAndStrength(
                updatedProduct.getBrandName(), updatedProduct.getGenericName(), updatedProduct.getStrength()).isPresent()) {
            throw new HealixException("Product with same brand name, generic name, and strength already exists", "DUPLICATE_PRODUCT");
        }

        product.setBrandName(updatedProduct.getBrandName());
        product.setGenericName(updatedProduct.getGenericName());
        product.setManufacturer(updatedProduct.getManufacturer());
        product.setHsnCode(updatedProduct.getHsnCode());
        product.setSchedule(updatedProduct.getSchedule());
        product.setDosageForm(updatedProduct.getDosageForm());
        product.setStrength(updatedProduct.getStrength());
        product.setPackSize(updatedProduct.getPackSize());
        product.setMrp(updatedProduct.getMrp());
        product.setPtr(updatedProduct.getPtr());
        product.setPurchaseRate(updatedProduct.getPurchaseRate());
        product.setGstRate(updatedProduct.getGstRate());
        product.setNarcotic(updatedProduct.isNarcotic());
        product.setColdChain(updatedProduct.isColdChain());
        product.setRackLocation(updatedProduct.getRackLocation());
        product.setStatus(updatedProduct.getStatus());

        validateProduct(product);
        resolveAndBindSuppliers(product, approvedSupplierIds, tenantId);

        return productRepository.save(product);
    }

    private void validateProduct(Product product) {
        if (product.getMrp() == null || product.getMrp().compareTo(BigDecimal.ZERO) <= 0) {
            throw new HealixException("MRP must be greater than zero", "INVALID_MRP");
        }
        if (product.getPtr() == null || product.getPtr().compareTo(BigDecimal.ZERO) <= 0) {
            throw new HealixException("PTR must be greater than zero", "INVALID_PTR");
        }
        if (product.getPurchaseRate() == null || product.getPurchaseRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new HealixException("Purchase rate must be greater than zero", "INVALID_PURCHASE_RATE");
        }

        // Business rule: MRP >= PTR
        if (product.getMrp().compareTo(product.getPtr()) < 0) {
            throw new HealixException("MRP cannot be less than PTR", "MRP_LESS_THAN_PTR");
        }
        // Business rule: PTR >= Purchase Rate
        if (product.getPtr().compareTo(product.getPurchaseRate()) < 0) {
            throw new HealixException("PTR cannot be less than Purchase Rate", "PTR_LESS_THAN_PURCHASE_RATE");
        }

        // GST Validation
        BigDecimal gst = product.getGstRate();
        boolean validGst = gst.compareTo(BigDecimal.ZERO) == 0 ||
                gst.compareTo(BigDecimal.valueOf(5.00)) == 0 ||
                gst.compareTo(BigDecimal.valueOf(12.00)) == 0 ||
                gst.compareTo(BigDecimal.valueOf(18.00)) == 0;
        if (!validGst) {
            throw new HealixException("GST rate must be 0%, 5%, 12%, or 18%", "INVALID_GST_RATE");
        }
    }

    private void resolveAndBindSuppliers(Product product, Set<UUID> approvedSupplierIds, UUID tenantId) {
        if (approvedSupplierIds == null || approvedSupplierIds.isEmpty()) {
            product.setApprovedSuppliers(new HashSet<>());
            return;
        }

        Set<Supplier> resolvedSuppliers = new HashSet<>();
        for (UUID supplierId : approvedSupplierIds) {
            Supplier supplier = supplierRepository.findById(supplierId)
                    .orElseThrow(() -> new HealixException("Supplier not found: " + supplierId, "SUPPLIER_NOT_FOUND"));
            
            // Security tenant check
            if (!supplier.getTenantId().equals(tenantId)) {
                throw new HealixException("Supplier does not belong to this tenant", "TENANT_MISMATCH");
            }
            resolvedSuppliers.add(supplier);
        }
        product.setApprovedSuppliers(resolvedSuppliers);
    }
}
