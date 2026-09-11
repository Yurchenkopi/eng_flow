package ru.yurch.engflow.repository;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yurch.engflow.model.SupplierInvoiceLine;

public interface SupplierInvoiceLineRepository extends JpaRepository<SupplierInvoiceLine, Long> {

    @EntityGraph(
            attributePaths = {
                    "supplierInvoice",
                    "procurementLine",
                    "procurementLine.projectItem",
                    "procurementLine.projectItem.catalogItem",
                    "procurementLine.projectItem.catalogItem.measurementUnit"})
    List<SupplierInvoiceLine> findBySupplierInvoiceIdOrderByIdAsc(Long supplierInvoiceId);

    boolean existsBySupplierInvoiceIdAndProcurementLineId(Long supplierInvoiceId, Long procurementLineId);

    boolean existsByProcurementLineId(Long procurementLineId);

    @Query("select coalesce(sum(line.quantity),0) from SupplierInvoiceLine line where "
            + "line.procurementLine.id=:procurementLineId")
    BigDecimal allocatedByProcurementLine(@Param("procurementLineId") Long procurementLineId);

    @Query("select coalesce(sum(line.quantity),0) from SupplierInvoiceLine line where "
            + "line.procurementLine.projectItem.id=:projectItemId and line.supplierInvoice.paymentSubmittedDate is "
            + "not null")
    BigDecimal orderedByProjectItem(@Param("projectItemId") Long projectItemId);

    @Query("select coalesce(sum(line.quantity),0) from SupplierInvoiceLine line where "
            + "line.supplierInvoice.procurement.id=:procurementId and line.supplierInvoice.paymentSubmittedDate is "
            + "not null")
    BigDecimal orderedByProcurement(@Param("procurementId") Long procurementId);
}
