package in.healix.modules.organization.mapper;

import in.healix.modules.organization.domain.Tenant;
import in.healix.modules.organization.web.dto.TenantDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TenantMapper {
    TenantDTO toDto(Tenant tenant);
    Tenant toEntity(TenantDTO dto);
    void updateEntityFromDto(TenantDTO dto, @MappingTarget Tenant tenant);
}
