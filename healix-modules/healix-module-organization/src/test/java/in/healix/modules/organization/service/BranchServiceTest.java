package in.healix.modules.organization.service;

import in.healix.core.tenant.TenantContext;
import in.healix.modules.organization.domain.Branch;
import in.healix.modules.organization.domain.event.BranchCreatedEvent;
import in.healix.modules.organization.repository.BranchRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BranchService branchService;

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
    void createBranch_ShouldSaveBranchAndPublishEvent() {
        // Arrange
        Branch branch = new Branch();
        branch.setName("Test Branch");
        branch.setGstin("36AAAAA1111A1Z1");
        branch.setStateCode("36");
        branch.setAddress("Test Address");

        UUID expectedBranchId = UUID.randomUUID();
        when(branchRepository.save(any(Branch.class))).thenAnswer(invocation -> {
            Branch arg = invocation.getArgument(0);
            arg.setId(expectedBranchId);
            return arg;
        });

        // Act
        Branch saved = branchService.createBranch(branch);

        // Assert
        assertNotNull(saved);
        assertEquals(expectedBranchId, saved.getId());
        assertEquals(UUID.fromString(tenantId), saved.getTenantId());

        verify(branchRepository, times(1)).save(branch);

        ArgumentCaptor<BranchCreatedEvent> eventCaptor = ArgumentCaptor.forClass(BranchCreatedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        BranchCreatedEvent publishedEvent = eventCaptor.getValue();
        assertNotNull(publishedEvent);
        assertEquals(UUID.fromString(tenantId), publishedEvent.tenantId());
        assertEquals(expectedBranchId, publishedEvent.branchId());
        assertEquals("Test Branch", publishedEvent.name());
        assertEquals("36AAAAA1111A1Z1", publishedEvent.gstin());
    }
}
