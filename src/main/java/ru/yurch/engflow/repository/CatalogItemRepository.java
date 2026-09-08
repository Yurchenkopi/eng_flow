package ru.yurch.engflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import ru.yurch.engflow.model.CatalogItem;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.Optional;

public interface CatalogItemRepository extends JpaRepository<CatalogItem, Long> {
    @Override @EntityGraph(attributePaths={"measurementUnit","itemSuppliers","itemSuppliers.supplier"}) List<CatalogItem> findAll(Sort sort);
    @Override @EntityGraph(attributePaths={"measurementUnit","itemSuppliers","itemSuppliers.supplier"}) Optional<CatalogItem> findById(Long id);
    @EntityGraph(attributePaths={"measurementUnit","itemSuppliers","itemSuppliers.supplier"})
    List<CatalogItem> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths={"measurementUnit","itemSuppliers","itemSuppliers.supplier"})
    @Query("""
            select item from CatalogItem item
            where lower(coalesce(item.designation, '')) like lower(concat('%', :query, '%'))
               or lower(item.name) like lower(concat('%', :query, '%'))
               or lower(coalesce(item.manufacturer, '')) like lower(concat('%', :query, '%'))
               or exists (select relation.id from ItemSupplier relation where relation.catalogItem=item and (lower(relation.supplier.name) like lower(concat('%', :query, '%')) or lower(coalesce(relation.supplier.shortName,'')) like lower(concat('%', :query, '%'))))
            """)
    List<CatalogItem> search(@Param("query") String query, Sort sort);

    @EntityGraph(attributePaths={"measurementUnit"})
    @Query("""
            select item from CatalogItem item
            where lower(coalesce(item.designation, '')) like lower(concat('%', :query, '%'))
               or lower(item.name) like lower(concat('%', :query, '%'))
               or lower(coalesce(item.manufacturer, '')) like lower(concat('%', :query, '%'))
               or exists (select relation.id from ItemSupplier relation where relation.catalogItem=item and (lower(relation.supplier.name) like lower(concat('%', :query, '%')) or lower(coalesce(relation.supplier.shortName,'')) like lower(concat('%', :query, '%'))))
            order by case when lower(coalesce(item.designation, '')) = lower(:query) then 0 else 1 end,
                     item.name, item.designation
            """)
    List<CatalogItem> autocomplete(@Param("query") String query, Pageable pageable);
}
