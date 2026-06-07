package in.healix.modules.organization.web;

import in.healix.modules.organization.domain.Branch;
import in.healix.modules.organization.mapper.BranchMapper;
import in.healix.modules.organization.service.BranchService;
import in.healix.modules.organization.web.dto.BranchDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/branches")
public class BranchController {

    private final BranchService branchService;
    private final BranchMapper branchMapper;

    public BranchController(BranchService branchService, BranchMapper branchMapper) {
        this.branchService = branchService;
        this.branchMapper = branchMapper;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<List<BranchDTO>> getAllBranches() {
        List<BranchDTO> list = branchService.getAllBranches().stream()
                .map(branchMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<BranchDTO> getBranchById(@PathVariable UUID id) {
        Branch branch = branchService.getBranchById(id);
        return ResponseEntity.ok(branchMapper.toDto(branch));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<BranchDTO> createBranch(@RequestBody BranchDTO branchDTO) {
        Branch branch = branchMapper.toEntity(branchDTO);
        Branch savedBranch = branchService.createBranch(branch);
        return ResponseEntity.status(HttpStatus.CREATED).body(branchMapper.toDto(savedBranch));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<BranchDTO> updateBranch(@PathVariable UUID id, @RequestBody BranchDTO branchDTO) {
        Branch branch = branchMapper.toEntity(branchDTO);
        Branch updatedBranch = branchService.updateBranch(id, branch);
        return ResponseEntity.ok(branchMapper.toDto(updatedBranch));
    }
}
