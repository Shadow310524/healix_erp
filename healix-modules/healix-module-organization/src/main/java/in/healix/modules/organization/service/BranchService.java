package in.healix.modules.organization.service;

import in.healix.core.exception.HealixException;
import in.healix.core.tenant.TenantContext;
import in.healix.modules.organization.domain.Branch;
import in.healix.modules.organization.domain.event.BranchCreatedEvent;
import in.healix.modules.organization.repository.BranchRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BranchService {

    private final BranchRepository branchRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BranchService(BranchRepository branchRepository, ApplicationEventPublisher eventPublisher) {
        this.branchRepository = branchRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }

    public Branch getBranchById(UUID id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new HealixException("Branch not found", "BRANCH_NOT_FOUND"));
    }

    @Transactional
    public Branch createBranch(Branch branch) {
        // Retrieve tenant context derived securely from authenticated principal
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new HealixException("Tenant context required to create branch", "TENANT_CONTEXT_MISSING");
        }
        branch.setTenantId(UUID.fromString(tenantId));
        Branch savedBranch = branchRepository.save(branch);

        // Publish domain event
        eventPublisher.publishEvent(new BranchCreatedEvent(
            UUID.randomUUID(),
            savedBranch.getTenantId(),
            savedBranch.getId(),
            savedBranch.getName(),
            savedBranch.getGstin()
        ));

        return savedBranch;
    }

    @Transactional
    public Branch updateBranch(UUID id, Branch updatedBranch) {
        Branch branch = getBranchById(id);
        branch.setName(updatedBranch.getName());
        branch.setGstin(updatedBranch.getGstin());
        branch.setStateCode(updatedBranch.getStateCode());
        branch.setAddress(updatedBranch.getAddress());
        branch.setStatus(updatedBranch.getStatus());
        return branchRepository.save(branch);
    }
}
