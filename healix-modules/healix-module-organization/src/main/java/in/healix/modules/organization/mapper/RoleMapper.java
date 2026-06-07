package in.healix.modules.organization.mapper;

import in.healix.modules.organization.domain.Role;
import in.healix.modules.organization.web.dto.RoleDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissionIds", ignore = true)
    RoleDTO toDto(Role role);

    @Mapping(target = "permissions", ignore = true)
    Role toEntity(RoleDTO dto);

    @Mapping(target = "permissions", ignore = true)
    void updateEntityFromDto(RoleDTO dto, @MappingTarget Role role);
}
