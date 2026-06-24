package in.healix.modules.catalog.mapper;

import in.healix.modules.catalog.domain.Product;
import in.healix.modules.catalog.domain.Supplier;
import in.healix.modules.catalog.web.dto.ProductDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "approvedSupplierIds", source = "approvedSuppliers", qualifiedByName = "mapSuppliersToIds")
    ProductDTO toDto(Product entity);

    @Mapping(target = "approvedSuppliers", ignore = true)
    Product toEntity(ProductDTO dto);

    @Mapping(target = "approvedSuppliers", ignore = true)
    void updateEntityFromDto(ProductDTO dto, @MappingTarget Product entity);

    @Named("mapSuppliersToIds")
    default Set<UUID> mapSuppliersToIds(Set<Supplier> suppliers) {
        if (suppliers == null) {
            return Collections.emptySet();
        }
        return suppliers.stream().map(Supplier::getId).collect(Collectors.toSet());
    }
}
