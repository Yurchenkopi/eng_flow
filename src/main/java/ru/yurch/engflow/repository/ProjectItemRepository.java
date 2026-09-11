package ru.yurch.engflow.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yurch.engflow.model.ProjectItem;

public interface ProjectItemRepository extends JpaRepository<ProjectItem, Long> {

    @EntityGraph(
            attributePaths = {
                    "catalogItem",
                    "catalogItem.measurementUnit",
                    "allocations",
                    "allocations.projectAssembly",
                    "allocations.projectSubsection"})
    List<ProjectItem> findByProjectIdOrderByIdAsc(Long projectId);

    @EntityGraph(
            attributePaths = {
                    "catalogItem",
                    "catalogItem.measurementUnit",
                    "allocations",
                    "allocations.projectAssembly",
                    "allocations.projectSubsection"})
    Optional<ProjectItem> findByIdAndProjectId(Long id, Long projectId);

    @EntityGraph(
            attributePaths = {
                    "catalogItem",
                    "catalogItem.measurementUnit",
                    "allocations",
                    "allocations.projectAssembly",
                    "allocations.projectSubsection"})
    @Query("""
            select distinct item from ProjectItem item
            where item.project.id = :projectId
              and (:assemblyId is null or exists (
                   select allocation.id from ProjectItemAllocation allocation
                   where allocation.projectItem = item and allocation.projectAssembly.id = :assemblyId))
              and (:supplierId is null or exists (
                   select itemSupplier.id from ItemSupplier itemSupplier
                   where itemSupplier.catalogItem = item.catalogItem and itemSupplier.supplier.id = :supplierId))
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
    List<ProjectItem> search(
            @Param("projectId") Long projectId,
            @Param("query") String query,
            @Param("assemblyId") Long assemblyId,
            @Param("supplierId") Long supplierId,
            Sort sort);

    @Query("select count(distinct item.project.id) from ProjectItem item where item.catalogItem.id = :catalogItemId")
    long countProjectsUsingCatalogItem(@Param("catalogItemId") Long catalogItemId);

    @Query("select coalesce(sum(allocation.quantity),0) from ProjectItemAllocation allocation where "
            + "allocation.projectItem.id = :projectItemId")
    java.math.BigDecimal requiredQuantity(@Param("projectItemId") Long projectItemId);

    Optional<ProjectItem> findByProjectIdAndCatalogItemId(Long projectId, Long catalogItemId);
}
