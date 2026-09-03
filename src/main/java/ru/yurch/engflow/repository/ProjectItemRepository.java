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
    @EntityGraph(attributePaths = {"catalogItem", "projectAssembly"})
    List<ProjectItem> findByProjectIdOrderByIdAsc(Long projectId);
    @EntityGraph(attributePaths = {"catalogItem", "projectAssembly"})
    Optional<ProjectItem> findByIdAndProjectId(Long id, Long projectId);

    @EntityGraph(attributePaths = {"catalogItem", "projectAssembly"})
    @Query("""
            select item from ProjectItem item
            left join item.projectAssembly assembly
            where item.project.id = :projectId
              and (:assemblyId is null or assembly.id = :assemblyId)
              and (:query = ''
                   or lower(coalesce(item.catalogItem.designation, '')) like lower(concat('%', :query, '%'))
                   or lower(item.catalogItem.name) like lower(concat('%', :query, '%'))
                   or lower(coalesce(item.catalogItem.manufacturer, '')) like lower(concat('%', :query, '%'))
                   or lower(coalesce(assembly.name, '')) like lower(concat('%', :query, '%'))
                   or lower(coalesce(item.notes, '')) like lower(concat('%', :query, '%')))
            """)
    List<ProjectItem> search(@Param("projectId") Long projectId, @Param("query") String query,
                             @Param("assemblyId") Long assemblyId, Sort sort);

    @Query("select count(distinct item.project.id) from ProjectItem item where item.catalogItem.id = :catalogItemId")
    long countProjectsUsingCatalogItem(@Param("catalogItemId") Long catalogItemId);
}
