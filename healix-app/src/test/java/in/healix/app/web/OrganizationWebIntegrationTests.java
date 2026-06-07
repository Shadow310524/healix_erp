package in.healix.app.web;

import in.healix.modules.organization.domain.Branch;
import in.healix.modules.organization.domain.Tenant;
import in.healix.modules.organization.mapper.*;
import in.healix.modules.organization.service.*;
import in.healix.modules.organization.web.*;
import in.healix.modules.organization.web.dto.BranchDTO;
import in.healix.modules.organization.web.dto.TenantDTO;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        BranchController.class,
        WarehouseController.class,
        TenantController.class,
        UserController.class,
        RoleController.class,
        PermissionController.class
})
@Import(SecurityConfiguration.class)
class OrganizationWebIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BranchService branchService;
    @MockBean
    private BranchMapper branchMapper;

    @MockBean
    private WarehouseService warehouseService;
    @MockBean
    private WarehouseMapper warehouseMapper;

    @MockBean
    private TenantService tenantService;
    @MockBean
    private TenantMapper tenantMapper;

    @MockBean
    private UserService userService;
    @MockBean
    private UserMapper userMapper;

    @MockBean
    private RoleService roleService;
    @MockBean
    private RoleMapper roleMapper;

    @MockBean
    private PermissionService permissionService;
    @MockBean
    private PermissionMapper permissionMapper;

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
    void getBranches_WithUserRole_ShouldReturnBranchesList() throws Exception {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = generateToken("user@healix.in", tenantId, userId, List.of("ROLE_USER"));

        Branch branch = new Branch();
        branch.setId(UUID.randomUUID());
        branch.setName("Indiranagar Branch");

        BranchDTO branchDTO = new BranchDTO();
        branchDTO.setId(branch.getId());
        branchDTO.setName(branch.getName());

        when(branchService.getAllBranches()).thenReturn(List.of(branch));
        when(branchMapper.toDto(any(Branch.class))).thenReturn(branchDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/branches")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Indiranagar Branch"));
    }

    @Test
    void createBranch_WithUserRole_ShouldReturnForbidden() throws Exception {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = generateToken("user@healix.in", tenantId, userId, List.of("ROLE_USER"));

        // Act & Assert (Role ROLE_USER is not authorized to POST branches, requires ROLE_TENANT_ADMIN)
        mockMvc.perform(post("/api/v1/branches")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Indiranagar Branch\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createBranch_WithTenantAdminRole_ShouldReturnCreated() throws Exception {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = generateToken("admin@healix.in", tenantId, userId, List.of("ROLE_TENANT_ADMIN"));

        Branch branch = new Branch();
        branch.setId(UUID.randomUUID());
        branch.setName("Indiranagar Branch");

        BranchDTO branchDTO = new BranchDTO();
        branchDTO.setId(branch.getId());
        branchDTO.setName(branch.getName());

        when(branchMapper.toEntity(any(BranchDTO.class))).thenReturn(branch);
        when(branchService.createBranch(any(Branch.class))).thenReturn(branch);
        when(branchMapper.toDto(any(Branch.class))).thenReturn(branchDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/branches")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Indiranagar Branch\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Indiranagar Branch"));
    }

    @Test
    void getTenants_WithPlatformAdminRole_ShouldReturnTenants() throws Exception {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = generateToken("super@healix.in", tenantId, userId, List.of("ROLE_PLATFORM_ADMIN"));

        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        tenant.setName("Healix Corp");

        TenantDTO tenantDTO = new TenantDTO();
        tenantDTO.setId(tenantId);
        tenantDTO.setName("Healix Corp");

        when(tenantService.getAllTenants()).thenReturn(List.of(tenant));
        when(tenantMapper.toDto(any(Tenant.class))).thenReturn(tenantDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/tenants")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Healix Corp"));
    }

    @Test
    void getTenants_WithTenantAdminRole_ShouldReturnForbidden() throws Exception {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = generateToken("admin@healix.in", tenantId, userId, List.of("ROLE_TENANT_ADMIN"));

        // Act & Assert (ROLE_TENANT_ADMIN is not authorized to list all tenants, requires ROLE_PLATFORM_ADMIN)
        mockMvc.perform(get("/api/v1/tenants")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
