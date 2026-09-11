package ru.yurch.engflow.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yurch.engflow.model.CatalogItemAnalog;

public interface CatalogItemAnalogRepository extends JpaRepository<CatalogItemAnalog, Long> {

    @EntityGraph(
            attributePaths = {
                    "firstCatalogItem",
                    "firstCatalogItem.measurementUnit",
                    "secondCatalogItem",
                    "secondCatalogItem.measurementUnit"})
    @Query("select relation from CatalogItemAnalog relation where relation.firstCatalogItem.id=:itemId or "
            + "relation.secondCatalogItem.id=:itemId order by relation.id")
    List<CatalogItemAnalog> findAllFor(@Param("itemId") Long itemId);

    Optional<CatalogItemAnalog> findByFirstCatalogItemIdAndSecondCatalogItemId(Long firstId, Long secondId);

    boolean existsByFirstCatalogItemIdAndSecondCatalogItemId(Long firstId, Long secondId);
}
