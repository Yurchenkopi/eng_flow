package ru.yurch.engflow.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurch.engflow.model.Procurement;
import java.util.List;
import java.util.Optional;

public interface ProcurementRepository extends JpaRepository<Procurement,Long> {
    @EntityGraph(attributePaths={"project","supplier"}) List<Procurement> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    @EntityGraph(attributePaths={"project","supplier"}) Optional<Procurement> findByIdAndProjectId(Long id,Long projectId);
}
