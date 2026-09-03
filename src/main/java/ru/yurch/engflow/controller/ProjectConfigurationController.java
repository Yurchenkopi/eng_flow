package ru.yurch.engflow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yurch.engflow.service.ProjectAssemblyService;
import ru.yurch.engflow.service.ProjectItemService;
import ru.yurch.engflow.service.ProjectService;

@Controller @RequestMapping("/projects/{projectId}/configuration")
public class ProjectConfigurationController {
    private final ProjectService projectService; private final ProjectItemService itemService; private final ProjectAssemblyService assemblyService;
    public ProjectConfigurationController(ProjectService projectService, ProjectItemService itemService, ProjectAssemblyService assemblyService) { this.projectService = projectService; this.itemService = itemService; this.assemblyService = assemblyService; }
    @GetMapping public String configuration(@PathVariable Long projectId, @RequestParam(required = false) String search,
            @RequestParam(required = false) Long assemblyId, @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction, Model model) {
        model.addAttribute("project", projectService.findById(projectId)); model.addAttribute("assemblies", assemblyService.findByProject(projectId));
        model.addAttribute("items", itemService.search(projectId, search, assemblyId, sort, direction));
        model.addAttribute("search", search); model.addAttribute("assemblyId", assemblyId); model.addAttribute("sort", sort); model.addAttribute("direction", direction);
        return "project-items/configuration";
    }
}
