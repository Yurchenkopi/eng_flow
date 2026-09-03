package ru.yurch.engflow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.model.*;
import ru.yurch.engflow.repository.ProjectItemRepository;
import java.util.List;
import org.springframework.data.domain.Sort;

@Service @Transactional(readOnly = true)
public class ProjectItemService {
    private final ProjectItemRepository repository; private final ProjectService projectService; private final CatalogItemService catalogItemService; private final ProjectAssemblyService assemblyService;
    public ProjectItemService(ProjectItemRepository repository, ProjectService projectService, CatalogItemService catalogItemService, ProjectAssemblyService assemblyService) { this.repository = repository; this.projectService = projectService; this.catalogItemService = catalogItemService; this.assemblyService = assemblyService; }
    public List<ProjectItem> findByProject(Long projectId) { return repository.findByProjectIdOrderByIdAsc(projectId); }
    public List<ProjectItem> search(Long projectId, String query, Long assemblyId, String sort, String direction) {
        String property = switch (sort == null ? "" : sort) {
            case "designation" -> "catalogItem.designation"; case "manufacturer" -> "catalogItem.manufacturer";
            case "quantity" -> "requiredQuantity"; case "assembly" -> "projectAssembly.name"; default -> "catalogItem.name";
        };
        Sort order = Sort.by(Sort.Direction.fromOptionalString(direction).orElse(Sort.Direction.ASC), property).and(Sort.by("id"));
        return repository.search(projectId, query == null ? "" : query.trim(), assemblyId, order);
    }
    public long countProjectsUsingCatalogItem(Long catalogItemId) { return repository.countProjectsUsingCatalogItem(catalogItemId); }
    public ProjectItem findByProjectAndId(Long projectId, Long id) { return repository.findByIdAndProjectId(id, projectId).orElseThrow(() -> new IllegalArgumentException("Позиция проекта не найдена: " + id)); }
    @Transactional public ProjectItem create(Long projectId, ProjectItem item) { item.setId(null); item.setProject(projectService.findById(projectId)); resolveReferences(projectId, item); return repository.save(item); }
    @Transactional public ProjectItem update(Long projectId, Long id, ProjectItem values) {
        ProjectItem item = findByProjectAndId(projectId, id); resolveReferences(projectId, values);
        item.setCatalogItem(values.getCatalogItem()); item.setProjectAssembly(values.getProjectAssembly()); item.setRequiredQuantity(values.getRequiredQuantity()); item.setNotes(values.getNotes()); return repository.save(item);
    }
    @Transactional public void delete(Long projectId, Long id) { repository.delete(findByProjectAndId(projectId, id)); }
    private void resolveReferences(Long projectId, ProjectItem item) {
        if (item.getCatalogItem() == null || item.getCatalogItem().getId() == null) throw new IllegalArgumentException("Выберите изделие");
        item.setCatalogItem(catalogItemService.findById(item.getCatalogItem().getId()));
        if (item.getProjectAssembly() != null && item.getProjectAssembly().getId() != null) item.setProjectAssembly(assemblyService.findByProjectAndId(projectId, item.getProjectAssembly().getId())); else item.setProjectAssembly(null);
    }
}
