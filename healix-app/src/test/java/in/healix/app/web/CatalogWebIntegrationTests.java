package in.healix.app.web;

import in.healix.modules.catalog.domain.Product;
import in.healix.modules.catalog.domain.Supplier;
import in.healix.modules.catalog.mapper.ProductMapper;
import in.healix.modules.catalog.mapper.SupplierMapper;
import in.healix.modules.catalog.service.ProductService;
import in.healix.modules.catalog.service.SupplierService;
import in.healix.modules.catalog.web.ProductController;
import in.healix.modules.catalog.web.SupplierController;
import in.healix.modules.catalog.web.dto.ProductDTO;
import in.healix.modules.catalog.web.dto.SupplierDTO;
import in.healix.security.config.SecurityConfiguration;
import in.healix.security.context.SecurityContextTenantProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        ProductController.class,
        SupplierController.class
})
@Import(SecurityConfiguration.class)
class CatalogWebIntegrationTests {

    @org.springframework.context.annotation.Configuration
    @org.springframework.boot.autoconfigure.EnableAutoConfiguration
    @org.springframework.context.annotation.Import({
            ProductController.class,
            SupplierController.class,
            SecurityConfiguration.class
    })
    static class TestConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;
    @MockBean
    private ProductMapper productMapper;

    @MockBean
    private SupplierService supplierService;
    @MockBean
    private SupplierMapper supplierMapper;

    @MockBean
    private SecurityContextTenantProvider securityContextTenantProvider;

    private final String secretString = "healix-secure-high-entropy-jwt-secret-key-32bytes-long";
    private final SecretKey key = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));

    private String generateToken(String email, UUID tenantId, UUID userId, List<String> roles) {
        return Jwts.builder()
                .subject(email)
                .claim("tenant_id", tenantId.toString())
                .claim("user_id", userId.toString())
                .claim("roles", roles)
                .signWith(key)
                .compact();
    }

    @Test
    void getProducts_WithUserRole_ShouldReturnProductsList() throws Exception {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = generateToken("user@healix.in", tenantId, userId, List.of("ROLE_USER"));

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setBrandName("Crocin");

        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setBrandName("Crocin");

        when(productService.getAllProducts()).thenReturn(List.of(product));
        when(productMapper.toDto(any(Product.class))).thenReturn(productDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].brandName").value("Crocin"));
    }

    @Test
    void createProduct_WithUserRole_ShouldReturnForbidden() throws Exception {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = generateToken("user@healix.in", tenantId, userId, List.of("ROLE_USER"));

        // Act & Assert (Role ROLE_USER cannot POST products, requires ROLE_TENANT_ADMIN)
        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"brandName\": \"Crocin\", \"genericName\": \"Paracetamol\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProduct_WithTenantAdminRole_ShouldReturnCreated() throws Exception {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = generateToken("admin@healix.in", tenantId, userId, List.of("ROLE_TENANT_ADMIN"));

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setBrandName("Crocin");

        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setBrandName("Crocin");
        productDTO.setApprovedSupplierIds(Set.of());

        when(productMapper.toEntity(any(ProductDTO.class))).thenReturn(product);
        when(productService.createProduct(any(Product.class), eq(Set.of()))).thenReturn(product);
        when(productMapper.toDto(any(Product.class))).thenReturn(productDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"brandName\": \"Crocin\", \"genericName\": \"Paracetamol\", \"approvedSupplierIds\": []}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.brandName").value("Crocin"));
    }

    @Test
    void getSuppliers_WithUserRole_ShouldReturnSuppliersList() throws Exception {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = generateToken("user@healix.in", tenantId, userId, List.of("ROLE_USER"));

        Supplier supplier = new Supplier();
        supplier.setId(UUID.randomUUID());
        supplier.setName("Astra Vendor");

        SupplierDTO supplierDTO = new SupplierDTO();
        supplierDTO.setId(supplier.getId());
        supplierDTO.setName("Astra Vendor");

        when(supplierService.getAllSuppliers()).thenReturn(List.of(supplier));
        when(supplierMapper.toDto(any(Supplier.class))).thenReturn(supplierDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/suppliers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Astra Vendor"));
    }

    @Test
    void createSupplier_WithUserRole_ShouldReturnForbidden() throws Exception {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = generateToken("user@healix.in", tenantId, userId, List.of("ROLE_USER"));

        // Act & Assert (Role ROLE_USER cannot POST suppliers, requires ROLE_TENANT_ADMIN)
        mockMvc.perform(post("/api/v1/suppliers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Astra Vendor\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createSupplier_WithTenantAdminRole_ShouldReturnCreated() throws Exception {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = generateToken("admin@healix.in", tenantId, userId, List.of("ROLE_TENANT_ADMIN"));

        Supplier supplier = new Supplier();
        supplier.setId(UUID.randomUUID());
        supplier.setName("Astra Vendor");

        SupplierDTO supplierDTO = new SupplierDTO();
        supplierDTO.setId(supplier.getId());
        supplierDTO.setName("Astra Vendor");

        when(supplierMapper.toEntity(any(SupplierDTO.class))).thenReturn(supplier);
        when(supplierService.createSupplier(any(Supplier.class))).thenReturn(supplier);
        when(supplierMapper.toDto(any(Supplier.class))).thenReturn(supplierDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/suppliers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Astra Vendor\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Astra Vendor"));
    }
}
