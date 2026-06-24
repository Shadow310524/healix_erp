package in.healix.modules.catalog.service;

import in.healix.core.exception.HealixException;
import in.healix.core.tenant.TenantContext;
import in.healix.modules.catalog.domain.Product;
import in.healix.modules.catalog.domain.Supplier;
import in.healix.modules.catalog.repository.ProductRepository;
import in.healix.modules.catalog.repository.SupplierRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private ProductService productService;

    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID().toString();
        TenantContext.setTenantId(tenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createProduct_WithValidData_ShouldSaveProduct() {
        // Arrange
        Product product = new Product();
        product.setBrandName("Crocin");
        product.setGenericName("Paracetamol");
        product.setManufacturer("GSK");
        product.setHsnCode("30049000");
        product.setStrength("650mg");
        product.setPackSize("Strip of 15 tablets");
        product.setMrp(BigDecimal.valueOf(30.00));
        product.setPtr(BigDecimal.valueOf(25.00));
        product.setPurchaseRate(BigDecimal.valueOf(22.00));
        product.setGstRate(BigDecimal.valueOf(12.00));

        when(productRepository.findByBrandNameAndGenericNameAndStrength(
                anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Product saved = productService.createProduct(product, Set.of());

        // Assert
        assertNotNull(saved);
        assertEquals("Crocin", saved.getBrandName());
        assertEquals(UUID.fromString(tenantId), saved.getTenantId());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void createProduct_WithMrpLessThanPtr_ShouldThrowException() {
        // Arrange
        Product product = new Product();
        product.setBrandName("Crocin");
        product.setGenericName("Paracetamol");
        product.setManufacturer("GSK");
        product.setHsnCode("30049000");
        product.setStrength("650mg");
        product.setPackSize("Strip of 15 tablets");
        product.setMrp(BigDecimal.valueOf(20.00)); // MRP = 20
        product.setPtr(BigDecimal.valueOf(25.00)); // PTR = 25 (Invalid: MRP < PTR)
        product.setPurchaseRate(BigDecimal.valueOf(18.00));
        product.setGstRate(BigDecimal.valueOf(12.00));

        when(productRepository.findByBrandNameAndGenericNameAndStrength(
                anyString(), anyString(), anyString())).thenReturn(Optional.empty());

        // Act & Assert
        HealixException ex = assertThrows(HealixException.class, () -> productService.createProduct(product, Set.of()));
        assertEquals("MRP_LESS_THAN_PTR", ex.getErrorCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_WithPtrLessThanPurchaseRate_ShouldThrowException() {
        // Arrange
        Product product = new Product();
        product.setBrandName("Crocin");
        product.setGenericName("Paracetamol");
        product.setManufacturer("GSK");
        product.setHsnCode("30049000");
        product.setStrength("650mg");
        product.setPackSize("Strip of 15 tablets");
        product.setMrp(BigDecimal.valueOf(30.00));
        product.setPtr(BigDecimal.valueOf(20.00)); // PTR = 20
        product.setPurchaseRate(BigDecimal.valueOf(22.00)); // PurchaseRate = 22 (Invalid: PTR < PurchaseRate)
        product.setGstRate(BigDecimal.valueOf(12.00));

        when(productRepository.findByBrandNameAndGenericNameAndStrength(
                anyString(), anyString(), anyString())).thenReturn(Optional.empty());

        // Act & Assert
        HealixException ex = assertThrows(HealixException.class, () -> productService.createProduct(product, Set.of()));
        assertEquals("PTR_LESS_THAN_PURCHASE_RATE", ex.getErrorCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_WithInvalidGstRate_ShouldThrowException() {
        // Arrange
        Product product = new Product();
        product.setBrandName("Crocin");
        product.setGenericName("Paracetamol");
        product.setManufacturer("GSK");
        product.setHsnCode("30049000");
        product.setStrength("650mg");
        product.setPackSize("Strip of 15 tablets");
        product.setMrp(BigDecimal.valueOf(30.00));
        product.setPtr(BigDecimal.valueOf(25.00));
        product.setPurchaseRate(BigDecimal.valueOf(22.00));
        product.setGstRate(BigDecimal.valueOf(7.00)); // Invalid GST (7%)

        when(productRepository.findByBrandNameAndGenericNameAndStrength(
                anyString(), anyString(), anyString())).thenReturn(Optional.empty());

        // Act & Assert
        HealixException ex = assertThrows(HealixException.class, () -> productService.createProduct(product, Set.of()));
        assertEquals("INVALID_GST_RATE", ex.getErrorCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_WithTenantMismatchSupplier_ShouldThrowException() {
        // Arrange
        Product product = new Product();
        product.setBrandName("Crocin");
        product.setGenericName("Paracetamol");
        product.setManufacturer("GSK");
        product.setHsnCode("30049000");
        product.setStrength("650mg");
        product.setPackSize("Strip of 15 tablets");
        product.setMrp(BigDecimal.valueOf(30.00));
        product.setPtr(BigDecimal.valueOf(25.00));
        product.setPurchaseRate(BigDecimal.valueOf(22.00));
        product.setGstRate(BigDecimal.valueOf(12.00));

        UUID supplierId = UUID.randomUUID();
        Supplier supplier = new Supplier();
        supplier.setId(supplierId);
        supplier.setTenantId(UUID.randomUUID()); // Different Tenant ID

        when(productRepository.findByBrandNameAndGenericNameAndStrength(
                anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));

        // Act & Assert
        HealixException ex = assertThrows(HealixException.class, () -> productService.createProduct(product, Set.of(supplierId)));
        assertEquals("TENANT_MISMATCH", ex.getErrorCode());
        verify(productRepository, never()).save(any());
    }
}
