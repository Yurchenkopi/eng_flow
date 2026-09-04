package ru.yurch.engflow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yurch.engflow.service.ProjectAssemblyService;
import ru.yurch.engflow.service.ProjectItemService;
import ru.yurch.engflow.service.ProjectService;
import ru.yurch.engflow.service.TransferActService;

@Controller @RequestMapping("/projects/{projectId}/configuration")
public class ProjectConfigurationController {
    private final ProjectService projectService; private final ProjectItemService itemService; private final ProjectAssemblyService assemblyService;private final TransferActService transferActs;
    public ProjectConfigurationController(ProjectService projectService, ProjectItemService itemService, ProjectAssemblyService assemblyService,TransferActService transferActs) { this.projectService = projectService; this.itemService = itemService; this.assemblyService = assemblyService;this.transferActs=transferActs; }
    @GetMapping public String configuration(@PathVariable Long projectId, @RequestParam(required = false) String search,
            @RequestParam(required = false) Long assemblyId, @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction, Model model) {
        model.addAttribute("project", projectService.findById(projectId)); model.addAttribute("assemblies", assemblyService.findByProject(projectId));
        var items=itemService.search(projectId, search, assemblyId, sort, direction);model.addAttribute("items",items);
        var transferred=transferActs.transferredByProject(projectId);model.addAttribute("transferred",transferred);
        model.addAttribute("remaining",items.stream().collect(java.util.stream.Collectors.toMap(ru.yurch.engflow.model.ProjectItem::getId,item->item.getRequiredQuantity().subtract(transferred.getOrDefault(item.getId(),java.math.BigDecimal.ZERO)).max(java.math.BigDecimal.ZERO))));
        model.addAttribute("search", search); model.addAttribute("assemblyId", assemblyId); model.addAttribute("sort", sort); model.addAttribute("direction", direction);
        return "project-items/configuration";
    }
}
