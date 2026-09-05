package ru.yurch.engflow.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurch.engflow.model.ProjectItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectItemRepository extends JpaRepository<ProjectItem, Long> {
    @EntityGraph(attributePaths = {"catalogItem", "catalogItem.itemSuppliers", "catalogItem.itemSuppliers.supplier", "allocations", "allocations.projectAssembly"})
    List<ProjectItem> findByProjectIdOrderByIdAsc(Long projectId);
    @EntityGraph(attributePaths = {"catalogItem", "catalogItem.itemSuppliers", "catalogItem.itemSuppliers.supplier", "allocations", "allocations.projectAssembly"})
    Optional<ProjectItem> findByIdAndProjectId(Long id, Long projectId);

    @EntityGraph(attributePaths = {"catalogItem", "catalogItem.itemSuppliers", "catalogItem.itemSuppliers.supplier", "allocations", "allocations.projectAssembly"})
    @Query("""
            select distinct item from ProjectItem item
            where item.project.id = :projectId
              and (:assemblyId is null or exists (
                   select allocation.id from ProjectItemAllocation allocation
                   where allocation.projectItem = item and allocation.projectAssembly.id = :assemblyId))
              and (:query = ''
                   or lower(coalesce(item.catalogItem.designation, '')) like lower(concat('%', :query, '%'))
                   or lower(item.catalogItem.name) like lower(concat('%', :query, '%'))
                   or lower(coalesce(item.catalogItem.manufacturer, '')) like lower(concat('%', :query, '%'))
                   or lower(coalesce(item.notes, '')) like lower(concat('%', :query, '%'))
                   or exists (select allocation.id from ProjectItemAllocation allocation
                              left join allocation.projectAssembly assembly
                              where allocation.projectItem = item
                                and (lower(coalesce(assembly.name, '')) like lower(concat('%', :query, '%'))
                                     or lower(coalesce(allocation.notes, '')) like lower(concat('%', :query, '%')))))
            """)
    List<ProjectItem> search(@Param("projectId") Long projectId, @Param("query") String query,
                             @Param("assemblyId") Long assemblyId, Sort sort);

    @Query("select count(distinct item.project.id) from ProjectItem item where item.catalogItem.id = :catalogItemId")
    long countProjectsUsingCatalogItem(@Param("catalogItemId") Long catalogItemId);
    Optional<ProjectItem> findByProjectIdAndCatalogItemId(Long projectId, Long catalogItemId);
}
