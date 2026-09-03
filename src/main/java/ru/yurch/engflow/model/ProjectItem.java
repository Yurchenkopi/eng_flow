package ru.yurch.engflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "project_items")
public class ProjectItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    @NotNull(message = "Выберите изделие")
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "catalog_item_id", nullable = false)
    private CatalogItem catalogItem;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "project_assembly_id")
    private ProjectAssembly projectAssembly;
    @NotNull(message = "Укажите количество") @DecimalMin(value = "0.0001", message = "Количество должно быть положительным") @Digits(integer = 15, fraction = 4)
    @Column(name = "required_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal requiredQuantity;
    @Column(columnDefinition = "text") private String notes;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Project getProject() { return project; } public void setProject(Project project) { this.project = project; }
    public CatalogItem getCatalogItem() { return catalogItem; } public void setCatalogItem(CatalogItem catalogItem) { this.catalogItem = catalogItem; }
    public ProjectAssembly getProjectAssembly() { return projectAssembly; } public void setProjectAssembly(ProjectAssembly projectAssembly) { this.projectAssembly = projectAssembly; }
    public BigDecimal getRequiredQuantity() { return requiredQuantity; } public void setRequiredQuantity(BigDecimal requiredQuantity) { this.requiredQuantity = requiredQuantity; }
    public String getNotes() { return notes; } public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; } public Instant getUpdatedAt() { return updatedAt; }
}
