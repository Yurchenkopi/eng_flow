package ru.yurch.engflow.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurch.engflow.model.ProjectItemAllocation;

public interface ProjectItemAllocationRepository extends JpaRepository<ProjectItemAllocation, Long> {

    @EntityGraph(
            attributePaths = {
                    "projectItem",
                    "projectItem.project",
                    "projectItem.catalogItem",
                    "projectItem.catalogItem.measurementUnit",
                    "projectAssembly",
                    "projectSubsection"})
    Optional<ProjectItemAllocation> findByIdAndProjectItemProjectId(Long id, Long projectId);
}
