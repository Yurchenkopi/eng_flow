package ru.yurch.engflow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yurch.engflow.service.ProjectService;
import ru.yurch.engflow.service.TransferActService;

@Controller
public class ProjectTransferActController {
    private final TransferActService transferActs;
    private final ProjectService projects;

    public ProjectTransferActController(TransferActService transferActs, ProjectService projects) {
        this.transferActs = transferActs;
        this.projects = projects;
    }

    @GetMapping("/projects/{projectId}/transfer-acts")
    public String list(@PathVariable Long projectId, Model model) {
        model.addAttribute("project", projects.findById(projectId));
        model.addAttribute("acts", transferActs.findByProject(projectId));
        return "transfer-acts/list";
    }
}
