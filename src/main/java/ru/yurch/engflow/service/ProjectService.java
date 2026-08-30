package ru.yurch.engflow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.model.Project;
import ru.yurch.engflow.model.ProjectStatus;
import ru.yurch.engflow.repository.ProjectRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> findAll() {
        return projectRepository.findAllByOrderByDesignationAsc();
    }

    public List<Project> findRecent() {
        return projectRepository.findTop5ByOrderByCreatedAtDesc();
    }

    public Project findById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Проект не найден: " + id));
    }

    public long countAll() {
        return projectRepository.count();
    }

    public long countByStatus(ProjectStatus status) {
        return projectRepository.countByStatus(status);
    }

    @Transactional
    public Project create(Project project) {
        normalizeDesignation(project);
        if (projectRepository.existsByDesignation(project.getDesignation())) {
            throw new DuplicateProjectDesignationException(project.getDesignation());
        }
        project.setId(null);
        return projectRepository.save(project);
    }

    @Transactional
    public Project update(Long id, Project values) {
        Project project = findById(id);
        normalizeDesignation(values);
        if (projectRepository.existsByDesignationAndIdNot(values.getDesignation(), id)) {
            throw new DuplicateProjectDesignationException(values.getDesignation());
        }

        project.setDesignation(values.getDesignation());
        project.setName(values.getName());
        project.setModificationName(values.getModificationName());
        project.setCustomer(values.getCustomer());
        project.setStatus(values.getStatus());
        project.setQuantity(values.getQuantity());
        project.setCompletionDate(values.getCompletionDate());
        project.setDescription(values.getDescription());
        project.setBasedOnProject(values.getBasedOnProject());
        return projectRepository.save(project);
    }

    private void normalizeDesignation(Project project) {
        if (project.getDesignation() != null) {
            project.setDesignation(project.getDesignation().trim());
        }
    }
}
