package ru.yurch.engflow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.yurch.engflow.model.ProjectStatus;
import ru.yurch.engflow.service.ProjectService;

@Controller
public class HomeController {

    private final ProjectService projectService;

    public HomeController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("totalProjects", projectService.countAll());
        model.addAttribute("designProjects", projectService.countByStatus(ProjectStatus.DESIGN));
        model.addAttribute("productionProjects", projectService.countByStatus(ProjectStatus.PRODUCTION));
        model.addAttribute("completedProjects", projectService.countByStatus(ProjectStatus.COMPLETED));
        model.addAttribute("recentProjects", projectService.findRecent());
        return "dashboard";
    }
}
