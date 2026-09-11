package ru.yurch.engflow.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yurch.engflow.model.ProcurementLine;

public interface ProcurementLineRepository extends JpaRepository<ProcurementLine, Long> {

    @EntityGraph(
            attributePaths = {
                    "projectItem",
                    "projectItem.catalogItem",
                    "projectItem.catalogItem.measurementUnit",
                    "projectItem.catalogItem.itemSuppliers",
                    "projectItem.catalogItem.itemSuppliers.supplier"})
    List<ProcurementLine> findByProcurementIdOrderByIdAsc(Long procurementId);

    @EntityGraph(
            attributePaths = {
                    "procurement",
                    "procurement.project",
                    "projectItem",
                    "projectItem.project",
                    "projectItem.catalogItem",
                    "projectItem.catalogItem.measurementUnit"})
    Optional<ProcurementLine> findByIdAndProcurementId(Long id, Long procurementId);

    boolean existsByProcurementIdAndProjectItemId(Long procurementId, Long projectItemId);

    boolean existsByIdAndProcurementRfqSentAtIsNull(Long id);

    boolean existsByProjectItemIdAndProcurementRfqSentAtIsNotNull(Long projectItemId);

    boolean existsByProjectItemId(Long projectItemId);

    long countByProcurementId(Long procurementId);

    void deleteByProcurementId(Long procurementId);

    @Query("select coalesce(sum(line.requestedQuantity),0) from ProcurementLine line where "
            + "line.projectItem.id=:projectItemId")
    BigDecimal requestedByProjectItem(@Param("projectItemId") Long projectItemId);

    @Query("select coalesce(sum(line.requestedQuantity),0) from ProcurementLine line where "
            + "line.projectItem.id=:projectItemId and line.procurement.rfqSentAt is not null")
    BigDecimal requestedSentByProjectItem(@Param("projectItemId") Long projectItemId);

    @Query("select coalesce(sum(line.requestedQuantity),0) from ProcurementLine line where "
            + "line.procurement.id=:procurementId")
    BigDecimal requestedByProcurement(@Param("procurementId") Long procurementId);
}
