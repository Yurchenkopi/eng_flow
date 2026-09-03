package ru.yurch.engflow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.model.ProjectAssembly;
import ru.yurch.engflow.repository.ProjectAssemblyRepository;
import java.util.List;

@Service @Transactional(readOnly = true)
public class ProjectAssemblyService {
    private final ProjectAssemblyRepository repository; private final ProjectService projectService;
    public ProjectAssemblyService(ProjectAssemblyRepository repository, ProjectService projectService) { this.repository = repository; this.projectService = projectService; }
    public List<ProjectAssembly> findByProject(Long projectId) { return repository.findByProjectIdOrderByNameAsc(projectId); }
    public ProjectAssembly findByProjectAndId(Long projectId, Long id) { return repository.findByIdAndProjectId(id, projectId).orElseThrow(() -> new IllegalArgumentException("Узел проекта не найден: " + id)); }
    @Transactional public ProjectAssembly create(Long projectId, ProjectAssembly assembly) {
        normalize(assembly); if (repository.existsByProjectIdAndNameIgnoreCase(projectId, assembly.getName())) throw new IllegalArgumentException("Узел с таким наименованием уже существует");
        assembly.setId(null); assembly.setProject(projectService.findById(projectId)); return repository.save(assembly);
    }
    @Transactional public ProjectAssembly update(Long projectId, Long id, ProjectAssembly values) {
        ProjectAssembly assembly = findByProjectAndId(projectId, id); normalize(values);
        if (repository.existsByProjectIdAndNameIgnoreCaseAndIdNot(projectId, values.getName(), id)) throw new IllegalArgumentException("Узел с таким наименованием уже существует");
        assembly.setName(values.getName()); assembly.setDesignation(values.getDesignation()); assembly.setNotes(values.getNotes()); return repository.save(assembly);
    }
    private void normalize(ProjectAssembly value) { value.setName(value.getName() == null ? null : value.getName().trim()); value.setDesignation(value.getDesignation() == null || value.getDesignation().isBlank() ? null : value.getDesignation().trim()); }
}
