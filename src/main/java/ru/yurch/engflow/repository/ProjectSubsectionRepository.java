package ru.yurch.engflow.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurch.engflow.model.ProjectSubsection;
import java.util.Optional;
public interface ProjectSubsectionRepository extends JpaRepository<ProjectSubsection,Long>{
 Optional<ProjectSubsection> findByProjectAssemblyIdAndDesignationIgnoreCase(Long assemblyId,String designation);
}
