package ru.yurch.engflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurch.engflow.model.ProjectAssembly;
import java.util.List;
import java.util.Optional;

public interface ProjectAssemblyRepository extends JpaRepository<ProjectAssembly, Long> {
    List<ProjectAssembly> findByProjectIdOrderByNameAsc(Long projectId);
    Optional<ProjectAssembly> findByIdAndProjectId(Long id, Long projectId);
    boolean existsByProjectIdAndNameIgnoreCase(Long projectId, String name);
    boolean existsByProjectIdAndNameIgnoreCaseAndIdNot(Long projectId, String name, Long id);
}
