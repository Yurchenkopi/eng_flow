package ru.yurch.engflow.controller;

import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yurch.engflow.model.Project;
import ru.yurch.engflow.model.ProjectStatus;
import ru.yurch.engflow.service.DuplicateProjectDesignationException;
import ru.yurch.engflow.service.OrganizationService;
import ru.yurch.engflow.service.ProjectService;
import ru.yurch.engflow.service.ProjectItemService;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final OrganizationService organizationService;
    private final ProjectItemService projectItemService;

    public ProjectController(ProjectService projectService, OrganizationService organizationService,
                             ProjectItemService projectItemService) {
        this.projectService = projectService;
        this.organizationService = organizationService;
        this.projectItemService = projectItemService;
    }

    @ModelAttribute
    public void formReferenceData(Model model) {
        model.addAttribute("statuses", ProjectStatus.values());
        model.addAttribute("organizations", organizationService.findAll());
        model.addAttribute("availableBaseProjects", projectService.findAll());
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("projects", projectService.findAll());
        return "projects/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("project", new Project());
        model.addAttribute("pageTitle", "Новый проект");
        return "projects/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute Project project,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Новый проект");
            return "projects/form";
        }
        try {
            Project savedProject = projectService.create(project);
            redirectAttributes.addFlashAttribute("successMessage", "Проект создан");
            return "redirect:/projects/" + savedProject.getId();
        } catch (DuplicateProjectDesignationException | DataIntegrityViolationException exception) {
            bindingResult.rejectValue("designation", "duplicate", "Проект с таким обозначением уже существует");
            model.addAttribute("pageTitle", "Новый проект");
            return "projects/form";
        }
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("project", projectService.findById(id));
        var items = projectItemService.findByProject(id);
        model.addAttribute("configurationCount", items.size());
        model.addAttribute("configurationPreview", items.stream().limit(5).toList());
        return "projects/details";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("project", projectService.findById(id));
        model.addAttribute("pageTitle", "Редактирование проекта");
        return "projects/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Project project,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            project.setId(id);
            model.addAttribute("pageTitle", "Редактирование проекта");
            return "projects/form";
        }
        try {
            projectService.update(id, project);
            redirectAttributes.addFlashAttribute("successMessage", "Проект обновлен");
            return "redirect:/projects/" + id;
        } catch (DuplicateProjectDesignationException | DataIntegrityViolationException exception) {
            project.setId(id);
            bindingResult.rejectValue("designation", "duplicate", "Проект с таким обозначением уже существует");
            model.addAttribute("pageTitle", "Редактирование проекта");
            return "projects/form";
        }
    }
}
