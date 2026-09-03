package ru.yurch.engflow.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yurch.engflow.model.*;
import ru.yurch.engflow.service.*;

@Controller @RequestMapping("/projects/{projectId}/items")
public class ProjectItemController {
    private final ProjectItemService service; private final ProjectService projectService; private final CatalogItemService catalogService; private final ProjectAssemblyService assemblyService;
    public ProjectItemController(ProjectItemService service, ProjectService projectService, CatalogItemService catalogService, ProjectAssemblyService assemblyService) { this.service = service; this.projectService = projectService; this.catalogService = catalogService; this.assemblyService = assemblyService; }
    @GetMapping("/new") public String createForm(@PathVariable Long projectId, @RequestParam(required = false) Long catalogItemId, Model model) {
        ProjectItem item = new ProjectItem(); item.setCatalogItem(catalogItemId == null ? new CatalogItem() : catalogService.findById(catalogItemId)); item.setProjectAssembly(new ProjectAssembly()); model.addAttribute("projectItem", item); formData(projectId, model, "Добавление изделия"); return "project-items/form";
    }
    @PostMapping public String create(@PathVariable Long projectId, @Valid @ModelAttribute ProjectItem projectItem, BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { formData(projectId, model, "Добавление изделия"); return "project-items/form"; }
        try { service.create(projectId, projectItem); } catch (IllegalArgumentException exception) { result.reject("invalidReferences", exception.getMessage()); formData(projectId, model, "Добавление изделия"); return "project-items/form"; }
        redirect.addFlashAttribute("successMessage", "Изделие добавлено в комплектацию"); return "redirect:/projects/" + projectId + "/configuration";
    }
    @GetMapping("/{id}/edit") public String editForm(@PathVariable Long projectId, @PathVariable Long id, Model model) { ProjectItem item = service.findByProjectAndId(projectId, id); if (item.getProjectAssembly() == null) item.setProjectAssembly(new ProjectAssembly()); model.addAttribute("projectItem", item); formData(projectId, model, "Редактирование позиции"); return "project-items/form"; }
    @PostMapping("/{id}") public String update(@PathVariable Long projectId, @PathVariable Long id, @Valid @ModelAttribute ProjectItem projectItem, BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { projectItem.setId(id); formData(projectId, model, "Редактирование позиции"); return "project-items/form"; }
        try { service.update(projectId, id, projectItem); } catch (IllegalArgumentException exception) { result.reject("invalidReferences", exception.getMessage()); formData(projectId, model, "Редактирование позиции"); return "project-items/form"; }
        redirect.addFlashAttribute("successMessage", "Позиция обновлена"); return "redirect:/projects/" + projectId + "/configuration";
    }
    @PostMapping("/{id}/delete") public String delete(@PathVariable Long projectId, @PathVariable Long id, RedirectAttributes redirect) { service.delete(projectId, id); redirect.addFlashAttribute("successMessage", "Позиция удалена"); return "redirect:/projects/" + projectId + "/configuration"; }
    private void formData(Long projectId, Model model, String title) { model.addAttribute("project", projectService.findById(projectId)); model.addAttribute("assemblies", assemblyService.findByProject(projectId)); model.addAttribute("pageTitle", title); }
}
