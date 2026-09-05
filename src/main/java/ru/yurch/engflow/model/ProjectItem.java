package ru.yurch.engflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
    @OneToMany(mappedBy = "projectItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<@Valid ProjectItemAllocation> allocations = new ArrayList<>();
    @Column(columnDefinition = "text") private String notes;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Project getProject() { return project; } public void setProject(Project project) { this.project = project; }
    public CatalogItem getCatalogItem() { return catalogItem; } public void setCatalogItem(CatalogItem catalogItem) { this.catalogItem = catalogItem; }
    public List<ProjectItemAllocation> getAllocations() { return allocations; }
    public void setAllocations(List<ProjectItemAllocation> allocations) { this.allocations = allocations == null ? new ArrayList<>() : allocations; }
    @Transient public BigDecimal getRequiredQuantity() { return allocations.stream().map(ProjectItemAllocation::getQuantity).filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add); }
    @Transient public String getSectionSummary(){int count=allocations.size();if(count==1){ProjectAssembly assembly=allocations.getFirst().getProjectAssembly();return assembly==null?"Без раздела":assembly.getName();}int mod100=count%100,mod10=count%10;String word=mod100>=11&&mod100<=14?"разделов":mod10==1?"раздел":mod10>=2&&mod10<=4?"раздела":"разделов";return count+" "+word;}
    public String getNotes() { return notes; } public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; } public Instant getUpdatedAt() { return updatedAt; }
}
