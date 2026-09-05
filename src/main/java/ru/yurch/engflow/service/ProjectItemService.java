package ru.yurch.engflow.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.model.*;
import ru.yurch.engflow.repository.ProjectItemRepository;
import java.util.*;

@Service @Transactional(readOnly = true)
public class ProjectItemService {
    private final ProjectItemRepository repository; private final ProjectService projectService; private final CatalogItemService catalogItemService; private final ProjectAssemblyService assemblyService;
    public ProjectItemService(ProjectItemRepository repository, ProjectService projectService, CatalogItemService catalogItemService, ProjectAssemblyService assemblyService) { this.repository = repository; this.projectService = projectService; this.catalogItemService = catalogItemService; this.assemblyService = assemblyService; }
    public List<ProjectItem> findByProject(Long projectId) { return repository.findByProjectIdOrderByIdAsc(projectId); }
    public Optional<ProjectItem> findExisting(Long projectId, Long catalogItemId) { return catalogItemId == null ? Optional.empty() : repository.findByProjectIdAndCatalogItemId(projectId, catalogItemId); }
    public List<ProjectItem> search(Long projectId, String query, Long assemblyId, String sort, String direction) {
        List<ProjectItem> result = repository.search(projectId, query == null ? "" : query.trim(), assemblyId, Sort.by("id"));
        Comparator<ProjectItem> comparator = switch (sort == null ? "" : sort) {
            case "designation" -> Comparator.comparing(i -> Objects.toString(i.getCatalogItem().getDesignation(), ""), String.CASE_INSENSITIVE_ORDER);
            case "manufacturer" -> Comparator.comparing(i -> Objects.toString(i.getCatalogItem().getManufacturer(), ""), String.CASE_INSENSITIVE_ORDER);
            case "quantity" -> Comparator.comparing(ProjectItem::getRequiredQuantity);
            case "assembly" -> Comparator.comparing(this::assemblyNames, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(i -> i.getCatalogItem().getName(), String.CASE_INSENSITIVE_ORDER);
        };
        if ("desc".equalsIgnoreCase(direction)) comparator = comparator.reversed();
        result.sort(comparator.thenComparing(ProjectItem::getId)); return result;
    }
    public long countProjectsUsingCatalogItem(Long catalogItemId) { return repository.countProjectsUsingCatalogItem(catalogItemId); }
    public java.time.Instant lastConfigurationChange(Long projectId){return findByProject(projectId).stream().flatMap(item->java.util.stream.Stream.concat(java.util.stream.Stream.of(item.getUpdatedAt()),item.getAllocations().stream().map(ProjectItemAllocation::getUpdatedAt))).filter(Objects::nonNull).max(java.time.Instant::compareTo).orElse(null);}
    public ProjectItem findByProjectAndId(Long projectId, Long id) { return repository.findByIdAndProjectId(id, projectId).orElseThrow(() -> new IllegalArgumentException("Позиция проекта не найдена: " + id)); }
    @Transactional public ProjectItem create(Long projectId, ProjectItem item) {
        item.setId(null); item.setProject(projectService.findById(projectId)); resolveReferences(projectId, item);
        findExisting(projectId, item.getCatalogItem().getId()).ifPresent(existing -> { throw new DuplicateProjectItemException(existing.getId()); });
        return repository.save(item);
    }
    @Transactional public ProjectItem update(Long projectId, Long id, ProjectItem values) {
        ProjectItem item = findByProjectAndId(projectId, id); resolveReferences(projectId, values);
        findExisting(projectId, values.getCatalogItem().getId()).filter(existing -> !existing.getId().equals(id)).ifPresent(existing -> { throw new DuplicateProjectItemException(existing.getId()); });
        item.setCatalogItem(values.getCatalogItem()); item.setNotes(values.getNotes()); Map<Long,ProjectItemAllocation> existing=new HashMap<>();item.getAllocations().forEach(allocation->existing.put(allocation.getId(),allocation));Set<Long> retained=new HashSet<>();
        for (ProjectItemAllocation source : values.getAllocations()) { if(source.getId()==null){source.setProjectItem(item);item.getAllocations().add(source);}else{ProjectItemAllocation target=existing.get(source.getId());if(target==null)throw new IllegalArgumentException("Распределение не принадлежит позиции проекта");target.setProjectAssembly(source.getProjectAssembly());target.setQuantity(source.getQuantity());target.setNotes(source.getNotes());retained.add(target.getId());} }
        item.getAllocations().removeIf(allocation->allocation.getId()!=null&&!retained.contains(allocation.getId()));
        return repository.save(item);
    }
    @Transactional public void delete(Long projectId, Long id) { repository.delete(findByProjectAndId(projectId, id)); }
    private void resolveReferences(Long projectId, ProjectItem item) {
        if (item.getCatalogItem() == null || item.getCatalogItem().getId() == null) throw new IllegalArgumentException("Выберите изделие");
        item.setCatalogItem(catalogItemService.findById(item.getCatalogItem().getId()));
        if (item.getAllocations() == null || item.getAllocations().isEmpty()) throw new IllegalArgumentException("Добавьте хотя бы одно распределение");
        for (ProjectItemAllocation allocation : item.getAllocations()) {
            if (allocation.getQuantity() == null || allocation.getQuantity().signum() <= 0) throw new IllegalArgumentException("Количество распределения должно быть положительным");
            if (allocation.getProjectAssembly() != null && allocation.getProjectAssembly().getId() != null) allocation.setProjectAssembly(assemblyService.findByProjectAndId(projectId, allocation.getProjectAssembly().getId())); else allocation.setProjectAssembly(null);
            allocation.setProjectItem(item);
            if(allocation.getQuantity().remainder(item.getCatalogItem().getQuantityStep()).signum()!=0)throw new IllegalArgumentException("Количество должно соответствовать шагу единицы измерения: "+item.getCatalogItem().getQuantityStep().stripTrailingZeros().toPlainString());
        }
        Set<Long> assemblyIds=new HashSet<>();boolean withoutAssembly=false;
        for(ProjectItemAllocation allocation:item.getAllocations()){
            Long assemblyId=allocation.getProjectAssembly()==null?null:allocation.getProjectAssembly().getId();
            if(assemblyId==null){if(withoutAssembly)throw new IllegalArgumentException("Раздел «Без раздела» может встречаться только один раз");withoutAssembly=true;}
            else if(!assemblyIds.add(assemblyId))throw new IllegalArgumentException("Один раздел может встречаться в распределении только один раз");
        }
    }
    private String assemblyNames(ProjectItem item) { return item.getAllocations().stream().map(ProjectItemAllocation::getProjectAssembly).filter(Objects::nonNull).map(ProjectAssembly::getName).sorted().reduce((a,b)->a+", "+b).orElse(""); }
    public static class DuplicateProjectItemException extends IllegalArgumentException { private final Long existingId; public DuplicateProjectItemException(Long existingId) { super("Это изделие уже добавлено в комплектацию"); this.existingId=existingId; } public Long getExistingId(){return existingId;} }
}
