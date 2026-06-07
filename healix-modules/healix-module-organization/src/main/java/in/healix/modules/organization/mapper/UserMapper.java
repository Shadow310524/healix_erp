package in.healix.modules.organization.mapper;

import in.healix.modules.organization.domain.User;
import in.healix.modules.organization.web.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roleIds", ignore = true)
    UserDTO toDto(User user);

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    User toEntity(UserDTO dto);

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    void updateEntityFromDto(UserDTO dto, @MappingTarget User user);
}
