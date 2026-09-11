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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

@Entity
@Table(
        name = "project_subsections",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_project_subsections_assembly_designation",
                columnNames = {
                        "project_assembly_id",
                        "designation"}))
public class ProjectSubsection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_assembly_id", nullable = false)
    private ProjectAssembly projectAssembly;
    @NotBlank
    @Column(nullable = false)
    private String designation;
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

    public void setId(Long id) {
        this.id = id;
    }

    public ProjectAssembly getProjectAssembly() {
        return projectAssembly;
    }

    public void setProjectAssembly(ProjectAssembly value) {
        projectAssembly = value;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String value) {
        designation = value;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
