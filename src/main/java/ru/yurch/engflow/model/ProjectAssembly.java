package ru.yurch.engflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "project_assemblies")
public class ProjectAssembly {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    @NotBlank(message = "Укажите наименование узла") @Size(max = 255)
    @Column(nullable = false) private String name;
    @Size(max = 255) private String designation;
    @Column(columnDefinition = "text") private String notes;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Project getProject() { return project; } public void setProject(Project project) { this.project = project; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getDesignation() { return designation; } public void setDesignation(String designation) { this.designation = designation; }
    public String getNotes() { return notes; } public void setNotes(String notes) { this.notes = notes; }
}
