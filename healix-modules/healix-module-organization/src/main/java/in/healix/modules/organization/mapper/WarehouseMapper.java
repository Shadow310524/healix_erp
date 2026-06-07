package in.healix.modules.organization.mapper;

import in.healix.modules.organization.domain.Warehouse;
import in.healix.modules.organization.web.dto.WarehouseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {
    @Mapping(source = "branch.id", target = "branchId")
    WarehouseDTO toDto(Warehouse warehouse);

    @Mapping(target = "branch", ignore = true)
    Warehouse toEntity(WarehouseDTO dto);

    @Mapping(target = "branch", ignore = true)
    void updateEntityFromDto(WarehouseDTO dto, @MappingTarget Warehouse warehouse);
}
