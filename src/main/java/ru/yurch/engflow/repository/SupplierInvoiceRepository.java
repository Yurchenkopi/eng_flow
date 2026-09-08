package ru.yurch.engflow.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurch.engflow.model.SupplierInvoice;
import java.util.List;
import java.util.Optional;

public interface SupplierInvoiceRepository extends JpaRepository<SupplierInvoice,Long> {
    @EntityGraph(attributePaths={"procurement","procurement.project","procurement.supplier"})
    List<SupplierInvoice> findByProcurementIdOrderByInvoiceDateDescIdDesc(Long procurementId);
    @EntityGraph(attributePaths={"procurement","procurement.project","procurement.supplier"})
    Optional<SupplierInvoice> findByIdAndProcurementId(Long id,Long procurementId);
    long countByProcurementId(Long procurementId);
}
