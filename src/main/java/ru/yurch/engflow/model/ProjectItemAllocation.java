package ru.yurch.engflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "project_item_allocations")
public class ProjectItemAllocation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "project_item_id", nullable = false) private ProjectItem projectItem;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "project_assembly_id") private ProjectAssembly projectAssembly;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="project_subsection_id") private ProjectSubsection projectSubsection;
    @Column(name="applies_for") private String appliesFor;
    @Transient private Boolean detailForTransfer;
    @Transient private String subsectionDesignation;
    @Transient private String subsectionAppliesFor;
    @NotNull(message = "Укажите количество") @DecimalMin(value = "0.0001", message = "Количество должно быть положительным") @Digits(integer = 15, fraction = 4)
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal quantity;
    @Column(columnDefinition = "text") private String notes;
    @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    @PrePersist void onCreate(){Instant now=Instant.now();createdAt=now;updatedAt=now;} @PreUpdate void onUpdate(){updatedAt=Instant.now();}
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public ProjectItem getProjectItem() { return projectItem; } public void setProjectItem(ProjectItem projectItem) { this.projectItem = projectItem; }
    public ProjectAssembly getProjectAssembly() { return projectAssembly; } public void setProjectAssembly(ProjectAssembly projectAssembly) { this.projectAssembly = projectAssembly; }
    public ProjectSubsection getProjectSubsection(){return projectSubsection;} public void setProjectSubsection(ProjectSubsection value){projectSubsection=value;}
    public boolean isDetailForTransfer(){return detailForTransfer!=null?detailForTransfer:projectSubsection!=null;} public void setDetailForTransfer(boolean value){detailForTransfer=value;}
    public String getSubsectionDesignation(){return projectSubsection!=null?projectSubsection.getDesignation():subsectionDesignation;} public void setSubsectionDesignation(String value){subsectionDesignation=value;}
    public String getSubsectionAppliesFor(){return appliesFor!=null?appliesFor:subsectionAppliesFor;} public void setSubsectionAppliesFor(String value){subsectionAppliesFor=value;appliesFor=value;}
    public String getAppliesFor(){return appliesFor;} public void setAppliesFor(String value){appliesFor=value;}
    public BigDecimal getQuantity() { return quantity; } public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getNotes() { return notes; } public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
