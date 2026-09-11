package ru.yurch.engflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "supplier_invoices")
public class SupplierInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "procurement_id", nullable = false)
    private Procurement procurement;
    @NotBlank
    @Column(name = "invoice_number", nullable = false)
    private String invoiceNumber;
    @NotNull
    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;
    @Column(name = "payment_submitted_date")
    private LocalDate paymentSubmittedDate;
    @Column(name = "file_original_name")
    private String fileOriginalName;
    @Column(name = "file_storage_name")
    private String fileStorageName;
    @Column(name = "file_content_type")
    private String fileContentType;
    @Column(columnDefinition = "text")
    private String notes;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long value) {
        id = value;
    }

    public Procurement getProcurement() {
        return procurement;
    }

    public void setProcurement(Procurement value) {
        procurement = value;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String value) {
        invoiceNumber = value;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate value) {
        invoiceDate = value;
    }

    public LocalDate getPaymentSubmittedDate() {
        return paymentSubmittedDate;
    }

    public void setPaymentSubmittedDate(LocalDate value) {
        paymentSubmittedDate = value;
    }

    public String getFileOriginalName() {
        return fileOriginalName;
    }

    public void setFileOriginalName(String value) {
        fileOriginalName = value;
    }

    public String getFileStorageName() {
        return fileStorageName;
    }

    public void setFileStorageName(String value) {
        fileStorageName = value;
    }

    public String getFileContentType() {
        return fileContentType;
    }

    public void setFileContentType(String value) {
        fileContentType = value;
    }

    public boolean hasFile() {
        return fileStorageName != null && !fileStorageName.isBlank();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String value) {
        notes = value;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
