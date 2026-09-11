package ru.yurch.engflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(
        name = "supplier_invoice_lines",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_invoice_lines_invoice_procurement_line",
                columnNames = {
                        "supplier_invoice_id",
                        "procurement_line_id"}))
public class SupplierInvoiceLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_invoice_id", nullable = false)
    private SupplierInvoice supplierInvoice;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "procurement_line_id", nullable = false)
    private ProcurementLine procurementLine;
    @NotNull
    @DecimalMin("0.0001")
    @Digits(integer = 15, fraction = 4)
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;
    @DecimalMin("0.00")
    @Digits(integer = 15, fraction = 2)
    @Column(name = "unit_price", precision = 19, scale = 2)
    private BigDecimal unitPrice;
    @Column(columnDefinition = "text")
    private String notes;

    public Long getId() {
        return id;
    }

    public void setId(Long value) {
        id = value;
    }

    public SupplierInvoice getSupplierInvoice() {
        return supplierInvoice;
    }

    public void setSupplierInvoice(SupplierInvoice value) {
        supplierInvoice = value;
    }

    public ProcurementLine getProcurementLine() {
        return procurementLine;
    }

    public void setProcurementLine(ProcurementLine value) {
        procurementLine = value;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal value) {
        quantity = value;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal value) {
        unitPrice = value;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String value) {
        notes = value;
    }
}
