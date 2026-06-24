package in.healix.modules.catalog.repository;

import in.healix.modules.catalog.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByBrandNameAndGenericNameAndStrength(String brandName, String genericName, String strength);
}
