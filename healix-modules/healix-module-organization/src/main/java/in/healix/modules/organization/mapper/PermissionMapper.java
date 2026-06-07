package in.healix.modules.organization.mapper;

import in.healix.modules.organization.domain.Permission;
import in.healix.modules.organization.web.dto.PermissionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionDTO toDto(Permission permission);
    Permission toEntity(PermissionDTO dto);
    void updateEntityFromDto(PermissionDTO dto, @MappingTarget Permission permission);
}
