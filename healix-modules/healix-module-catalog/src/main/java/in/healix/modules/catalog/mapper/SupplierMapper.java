package in.healix.modules.catalog.mapper;

import in.healix.modules.catalog.domain.Supplier;
import in.healix.modules.catalog.web.dto.SupplierDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    SupplierDTO toDto(Supplier entity);
    Supplier toEntity(SupplierDTO dto);
    void updateEntityFromDto(SupplierDTO dto, @MappingTarget Supplier entity);
}
