package in.healix.app.web;

import in.healix.modules.inventory.domain.InventoryBatch;
import in.healix.modules.inventory.domain.StockTransfer;
import in.healix.modules.inventory.domain.enums.TransferStatus;
import in.healix.modules.inventory.mapper.InventoryMapper;
import in.healix.modules.inventory.service.InventoryService;
import in.healix.modules.inventory.service.StockTransferService;
import in.healix.modules.inventory.web.InventoryController;
import in.healix.modules.inventory.web.StockTransferController;
import in.healix.modules.inventory.web.dto.InventoryBatchDTO;
import in.healix.modules.inventory.web.dto.StockTransferDTO;
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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        InventoryController.class,
        StockTransferController.class
})
@Import(SecurityConfiguration.class)
class InventoryWebIntegrationTests {

    @org.springframework.context.annotation.Configuration
    @org.springframework.boot.autoconfigure.EnableAutoConfiguration
    @org.springframework.context.annotation.Import({
            InventoryController.class,
            StockTransferController.class,
            SecurityConfiguration.class
    })
    static class TestConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private StockTransferService transferService;

    @MockBean
    private InventoryMapper inventoryMapper;

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
    void getAvailableStock_WithUserRole_ShouldReturnStockValue() throws Exception {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = generateToken("user@healix.in", tenantId, userId, List.of("ROLE_USER"));

        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();

        when(inventoryService.getAvailableStock(productId, warehouseId)).thenReturn(45);

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventory/stock")
                        .header("Authorization", "Bearer " + token)
                        .param("productId", productId.toString())
                        .param("warehouseId", warehouseId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(45));
    }

    @Test
    void createBatch_WithUserRole_ShouldReturnForbidden() throws Exception {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = generateToken("user@healix.in", tenantId, userId, List.of("ROLE_USER"));

        // Act & Assert (Role ROLE_USER cannot POST batches, requires ROLE_TENANT_ADMIN)
        mockMvc.perform(post("/api/v1/inventory/batches")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"batchNo\": \"B123\", \"quantity\": 100}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createBatch_WithTenantAdminRole_ShouldReturnCreated() throws Exception {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = generateToken("admin@healix.in", tenantId, userId, List.of("ROLE_TENANT_ADMIN"));

        InventoryBatch batch = new InventoryBatch();
        batch.setId(UUID.randomUUID());
        batch.setBatchNo("B123");
        batch.setQuantity(100);

        InventoryBatchDTO batchDTO = new InventoryBatchDTO();
        batchDTO.setId(batch.getId());
        batchDTO.setBatchNo("B123");
        batchDTO.setQuantity(100);

        when(inventoryMapper.toEntity(any(InventoryBatchDTO.class))).thenReturn(batch);
        when(inventoryService.createBatch(any(InventoryBatch.class))).thenReturn(batch);
        when(inventoryMapper.toDto(any(InventoryBatch.class))).thenReturn(batchDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/inventory/batches")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"batchNo\": \"B123\", \"quantity\": 100, \"mfgDate\": \"2026-05-01\", \"expiryDate\": \"2027-05-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.batchNo").value("B123"))
                .andExpect(jsonPath("$.quantity").value(100));
    }

    @Test
    void initiateTransfer_WithTenantAdminRole_ShouldReturnCreated() throws Exception {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = generateToken("admin@healix.in", tenantId, userId, List.of("ROLE_TENANT_ADMIN"));

        StockTransfer transfer = new StockTransfer();
        transfer.setId(UUID.randomUUID());
        transfer.setQuantity(50);
        transfer.setStatus(TransferStatus.REQUESTED);

        StockTransferDTO transferDTO = new StockTransferDTO();
        transferDTO.setId(transfer.getId());
        transferDTO.setQuantity(50);
        transferDTO.setStatus(TransferStatus.REQUESTED);

        when(inventoryMapper.toEntity(any(StockTransferDTO.class))).thenReturn(transfer);
        when(transferService.initiateTransfer(any(StockTransfer.class))).thenReturn(transfer);
        when(inventoryMapper.toDto(any(StockTransfer.class))).thenReturn(transferDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/inventory/transfers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceWarehouseId\": \"" + UUID.randomUUID() + "\", \"targetWarehouseId\": \"" + UUID.randomUUID() + "\", \"quantity\": 50}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(50))
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }
}
