package in.healix.modules.catalog.service;

import in.healix.core.exception.HealixException;
import in.healix.core.tenant.TenantContext;
import in.healix.modules.catalog.domain.Supplier;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierService supplierService;

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
    void createSupplier_WithValidData_ShouldSaveSupplier() {
        // Arrange
        Supplier supplier = new Supplier();
        supplier.setName("Pharma Supplier");
        supplier.setGstin("36AAAAA1111A1Z1");
        supplier.setCreditDays(30);
        supplier.setCreditLimit(BigDecimal.valueOf(100000));

        when(supplierRepository.findByName(supplier.getName())).thenReturn(Optional.empty());
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Supplier saved = supplierService.createSupplier(supplier);

        // Assert
        assertNotNull(saved);
        assertEquals(supplier.getName(), saved.getName());
        assertEquals(UUID.fromString(tenantId), saved.getTenantId());
        verify(supplierRepository, times(1)).save(supplier);
    }

    @Test
    void createSupplier_WithDuplicateName_ShouldThrowException() {
        // Arrange
        Supplier supplier = new Supplier();
        supplier.setName("Pharma Supplier");

        when(supplierRepository.findByName(supplier.getName())).thenReturn(Optional.of(supplier));

        // Act & Assert
        HealixException ex = assertThrows(HealixException.class, () -> supplierService.createSupplier(supplier));
        assertEquals("DUPLICATE_SUPPLIER_NAME", ex.getErrorCode());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void createSupplier_WithInvalidGstinLength_ShouldThrowException() {
        // Arrange
        Supplier supplier = new Supplier();
        supplier.setName("Pharma Supplier");
        supplier.setGstin("123"); // Invalid length

        when(supplierRepository.findByName(supplier.getName())).thenReturn(Optional.empty());

        // Act & Assert
        HealixException ex = assertThrows(HealixException.class, () -> supplierService.createSupplier(supplier));
        assertEquals("INVALID_GSTIN", ex.getErrorCode());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void createSupplier_WithNegativeCreditDays_ShouldThrowException() {
        // Arrange
        Supplier supplier = new Supplier();
        supplier.setName("Pharma Supplier");
        supplier.setCreditDays(-5); // Invalid credit days

        when(supplierRepository.findByName(supplier.getName())).thenReturn(Optional.empty());

        // Act & Assert
        HealixException ex = assertThrows(HealixException.class, () -> supplierService.createSupplier(supplier));
        assertEquals("INVALID_CREDIT_DAYS", ex.getErrorCode());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void createSupplier_WithNegativeCreditLimit_ShouldThrowException() {
        // Arrange
        Supplier supplier = new Supplier();
        supplier.setName("Pharma Supplier");
        supplier.setCreditLimit(BigDecimal.valueOf(-100)); // Invalid limit

        when(supplierRepository.findByName(supplier.getName())).thenReturn(Optional.empty());

        // Act & Assert
        HealixException ex = assertThrows(HealixException.class, () -> supplierService.createSupplier(supplier));
        assertEquals("INVALID_CREDIT_LIMIT", ex.getErrorCode());
        verify(supplierRepository, never()).save(any());
    }
}
