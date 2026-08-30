package ru.yurch.engflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import ru.yurch.engflow.model.Project;
import ru.yurch.engflow.model.ProjectStatus;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    boolean existsByDesignation(String designation);

    boolean existsByDesignationAndIdNot(String designation, Long id);

    long countByStatus(ProjectStatus status);

    @EntityGraph(attributePaths = {"customer", "basedOnProject"})
    List<Project> findTop5ByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"customer", "basedOnProject"})
    List<Project> findAllByOrderByDesignationAsc();

    @Override
    @EntityGraph(attributePaths = {"customer", "basedOnProject"})
    Optional<Project> findById(Long id);
}
