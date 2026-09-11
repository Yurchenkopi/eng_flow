package ru.yurch.engflow.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurch.engflow.model.ItemSupplier;

public interface ItemSupplierRepository extends JpaRepository<ItemSupplier, Long> {

    @Override
    @EntityGraph(attributePaths = {"catalogItem", "supplier"})
    Optional<ItemSupplier> findById(Long id);

    @EntityGraph(attributePaths = {"supplier"})
    List<ItemSupplier> findByCatalogItemIdOrderBySupplierNameAsc(Long catalogItemId);

    boolean existsByCatalogItemIdAndSupplierId(Long catalogItemId, Long supplierId);
}
