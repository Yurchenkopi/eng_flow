package ru.yurch.engflow.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurch.engflow.model.Project;
import ru.yurch.engflow.model.ProjectStatus;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select project from Project project where project.id=:id")
    Optional<Project> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);

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
