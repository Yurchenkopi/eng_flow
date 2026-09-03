package ru.yurch.engflow.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yurch.engflow.model.ProjectAssembly;
import ru.yurch.engflow.service.ProjectAssemblyService;
import ru.yurch.engflow.service.ProjectService;

@Controller @RequestMapping("/projects/{projectId}/assemblies")
public class ProjectAssemblyController {
    private final ProjectAssemblyService service; private final ProjectService projectService;
    public ProjectAssemblyController(ProjectAssemblyService service, ProjectService projectService) { this.service = service; this.projectService = projectService; }
    @GetMapping public String list(@PathVariable Long projectId, Model model) { model.addAttribute("project", projectService.findById(projectId)); model.addAttribute("assemblies", service.findByProject(projectId)); return "project-assemblies/list"; }
    @GetMapping("/new") public String createForm(@PathVariable Long projectId, Model model) { model.addAttribute("assembly", new ProjectAssembly()); formData(projectId, model, "Новый раздел"); return "project-assemblies/form"; }
    @PostMapping public String create(@PathVariable Long projectId, @Valid @ModelAttribute("assembly") ProjectAssembly assembly, BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { formData(projectId, model, "Новый раздел"); return "project-assemblies/form"; }
        try { service.create(projectId, assembly); } catch (IllegalArgumentException exception) { result.rejectValue("name", "duplicate", exception.getMessage()); formData(projectId, model, "Новый раздел"); return "project-assemblies/form"; }
        redirect.addFlashAttribute("successMessage", "Раздел добавлен"); return "redirect:/projects/" + projectId + "/assemblies";
    }
    @GetMapping("/{id}/edit") public String editForm(@PathVariable Long projectId, @PathVariable Long id, Model model) { model.addAttribute("assembly", service.findByProjectAndId(projectId, id)); formData(projectId, model, "Редактирование раздела"); return "project-assemblies/form"; }
    @PostMapping("/{id}") public String update(@PathVariable Long projectId, @PathVariable Long id, @Valid @ModelAttribute("assembly") ProjectAssembly assembly, BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { assembly.setId(id); formData(projectId, model, "Редактирование раздела"); return "project-assemblies/form"; }
        try { service.update(projectId, id, assembly); } catch (IllegalArgumentException exception) { result.rejectValue("name", "duplicate", exception.getMessage()); formData(projectId, model, "Редактирование раздела"); return "project-assemblies/form"; }
        redirect.addFlashAttribute("successMessage", "Раздел обновлен"); return "redirect:/projects/" + projectId + "/assemblies";
    }
    private void formData(Long projectId, Model model, String title) { model.addAttribute("project", projectService.findById(projectId)); model.addAttribute("pageTitle", title); }
}
