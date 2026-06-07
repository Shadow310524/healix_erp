package in.healix.modules.organization.mapper;

import in.healix.modules.organization.domain.Branch;
import in.healix.modules.organization.web.dto.BranchDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BranchMapper {
    BranchDTO toDto(Branch branch);
    Branch toEntity(BranchDTO dto);
    void updateEntityFromDto(BranchDTO dto, @MappingTarget Branch branch);
}
