package ru.yurch.engflow.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurch.engflow.model.Procurement;
import java.util.List;
import java.util.Optional;

public interface ProcurementRepository extends JpaRepository<Procurement,Long> {
    @EntityGraph(attributePaths={"project","supplier"}) @org.springframework.data.jpa.repository.Query("select procurement from Procurement procurement where procurement.project.id=:projectId and procurement.deletedAt is null order by procurement.createdAt desc") List<Procurement> findActiveByProjectId(@org.springframework.data.repository.query.Param("projectId") Long projectId);
    @EntityGraph(attributePaths={"project","supplier"}) @org.springframework.data.jpa.repository.Query("select procurement from Procurement procurement where procurement.id=:id and procurement.project.id=:projectId and procurement.deletedAt is null") Optional<Procurement> findActiveByIdAndProjectId(@org.springframework.data.repository.query.Param("id") Long id,@org.springframework.data.repository.query.Param("projectId") Long projectId);
    @org.springframework.data.jpa.repository.Query("select coalesce(max(procurement.sequenceNumber),0) from Procurement procurement where procurement.project.id=:projectId and procurement.supplier.id=:supplierId") Integer maxSequenceNumber(@org.springframework.data.repository.query.Param("projectId") Long projectId,@org.springframework.data.repository.query.Param("supplierId") Long supplierId);
}
