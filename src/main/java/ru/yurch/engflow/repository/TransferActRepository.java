package ru.yurch.engflow.repository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurch.engflow.model.TransferAct;
import java.util.List;
import java.util.Optional;
public interface TransferActRepository extends JpaRepository<TransferAct,Long>{
    @EntityGraph(attributePaths={"project","items","items.projectItemAllocation","items.projectItemAllocation.projectItem","items.projectItemAllocation.projectItem.catalogItem","items.projectItemAllocation.projectItem.catalogItem.measurementUnit","items.projectItemAllocation.projectAssembly","items.projectItemAllocation.projectSubsection"}) List<TransferAct> findAllByOrderByYearDescNumberDesc();
    @Override @EntityGraph(attributePaths={"project","items","items.projectItemAllocation","items.projectItemAllocation.projectItem","items.projectItemAllocation.projectItem.catalogItem","items.projectItemAllocation.projectItem.catalogItem.measurementUnit","items.projectItemAllocation.projectAssembly","items.projectItemAllocation.projectSubsection"}) Optional<TransferAct> findById(Long id);
}
