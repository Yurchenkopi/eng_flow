package ru.yurch.engflow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yurch.engflow.service.OrganizationService;
import ru.yurch.engflow.service.ProjectAssemblyService;
import ru.yurch.engflow.service.ProjectItemService;
import ru.yurch.engflow.service.ProjectService;
import ru.yurch.engflow.service.TransferActService;

@Controller
@RequestMapping("/projects/{projectId}/configuration")
public class ProjectConfigurationController {

    private final ProjectService projectService;
    private final ProjectItemService itemService;
    private final ProjectAssemblyService assemblyService;
    private final TransferActService transferActs;
    private final ru.yurch.engflow.service.ProcurementService procurements;
    private final OrganizationService organizations;

    public ProjectConfigurationController(
            ProjectService projectService,
            ProjectItemService itemService,
            ProjectAssemblyService assemblyService,
            TransferActService transferActs,
            ru.yurch.engflow.service.ProcurementService procurements,
            OrganizationService organizations) {
        this.projectService = projectService;
        this.itemService = itemService;
        this.assemblyService = assemblyService;
        this.transferActs = transferActs;
        this.procurements = procurements;
        this.organizations = organizations;
    }

    @GetMapping
    public String configuration(
            @PathVariable Long projectId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long assemblyId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long createSupplierId,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {
        model.addAttribute("project", projectService.findById(projectId));
        model.addAttribute("assemblies", assemblyService.findByProject(projectId));
        var items = itemService.search(projectId, search, assemblyId, supplierId, sort, direction);
        model.addAttribute("items", items);
        model.addAttribute("suppliers", organizations.findSuppliers());
        var transferred = transferActs.transferredByProject(projectId);
        model.addAttribute("transferred", transferred);
        model.addAttribute("procurementProgress", procurements.progressFor(items));
        model.addAttribute("parentTransferred",
                items.stream()
                        .collect(java.util.stream.Collectors.toMap(ru.yurch.engflow.model.ProjectItem::getId,
                                item -> item.getAllocations().stream().filter(allocation -> allocation.getProjectSubsection() != null)
                                        .map(allocation -> transferred.getOrDefault(allocation.getId(), java.math.BigDecimal.ZERO))
                                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add))));
        model.addAttribute("parentWorkshopTotal",
                items.stream()
                        .collect(java.util.stream.Collectors.toMap(ru.yurch.engflow.model.ProjectItem::getId,
                                item -> item.getAllocations().stream().filter(allocation -> allocation.getProjectSubsection() != null)
                                        .map(ru.yurch.engflow.model.ProjectItemAllocation::getQuantity)
                                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add))));
        model.addAttribute("remaining",
                items.stream().flatMap(item -> item.getAllocations().stream())
                        .collect(java.util.stream.Collectors.toMap(ru.yurch.engflow.model.ProjectItemAllocation::getId,
                                allocation -> allocation.getQuantity()
                                        .subtract(transferred.getOrDefault(allocation.getId(), java.math.BigDecimal.ZERO))
                                        .max(java.math.BigDecimal.ZERO))));
        model.addAttribute("search", search);
        model.addAttribute("assemblyId", assemblyId);
        model.addAttribute("supplierId", supplierId);
        model.addAttribute("createSupplierId", createSupplierId != null ? createSupplierId : supplierId);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        return "project-items/configuration";
    }
}
