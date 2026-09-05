package ru.yurch.engflow.repository;
import org.springframework.data.jpa.repository.EntityGraph;import org.springframework.data.jpa.repository.JpaRepository;import ru.yurch.engflow.model.ItemSupplier;import java.util.List;
public interface ItemSupplierRepository extends JpaRepository<ItemSupplier,Long>{
 @EntityGraph(attributePaths={"supplier"}) List<ItemSupplier> findByCatalogItemIdOrderBySupplierNameAsc(Long catalogItemId);
 boolean existsByCatalogItemIdAndSupplierId(Long catalogItemId,Long supplierId);
}
