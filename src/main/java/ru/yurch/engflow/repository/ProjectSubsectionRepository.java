package ru.yurch.engflow.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurch.engflow.model.ProjectSubsection;

public interface ProjectSubsectionRepository extends JpaRepository<ProjectSubsection, Long> {

    Optional<ProjectSubsection> findByProjectAssemblyIdAndDesignationIgnoreCase(Long assemblyId, String designation);
}
