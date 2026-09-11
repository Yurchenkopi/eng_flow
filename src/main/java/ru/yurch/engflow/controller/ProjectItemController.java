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
    private final ProjectItemService service; private final ProjectService projectService; private final CatalogItemService catalogService; private final ProjectAssemblyService assemblyService;private final CatalogItemAnalogService analogs;
    public ProjectItemController(ProjectItemService service, ProjectService projectService, CatalogItemService catalogService, ProjectAssemblyService assemblyService,CatalogItemAnalogService analogs) { this.service = service; this.projectService = projectService; this.catalogService = catalogService; this.assemblyService = assemblyService;this.analogs=analogs; }
    @GetMapping("/new") public String createForm(@PathVariable Long projectId, @RequestParam(required = false) Long catalogItemId, Model model) {
        if (catalogItemId != null) { var existing=service.findExisting(projectId,catalogItemId); if(existing.isPresent()) return "redirect:/projects/"+projectId+"/items/"+existing.get().getId()+"/edit?alreadyExists"; }
        ProjectItem item = new ProjectItem(); item.setCatalogItem(catalogItemId == null ? new CatalogItem() : catalogService.findById(catalogItemId)); item.getAllocations().add(blankAllocation()); model.addAttribute("projectItem", item); formData(projectId, model, "Добавление изделия"); return "project-items/form";
    }
    @PostMapping public String create(@PathVariable Long projectId, @Valid @ModelAttribute ProjectItem projectItem, BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { formData(projectId, model, "Добавление изделия"); return "project-items/form"; }
        try { service.create(projectId, projectItem); } catch (ProjectItemService.DuplicateProjectItemException exception) { redirect.addFlashAttribute("successMessage", "Изделие уже есть в комплектации — открыто существующее распределение"); return "redirect:/projects/"+projectId+"/items/"+exception.getExistingId()+"/edit"; } catch (IllegalArgumentException exception) { result.reject("invalidReferences", exception.getMessage()); formData(projectId, model, "Добавление изделия"); return "project-items/form"; }
        redirect.addFlashAttribute("successMessage", "Изделие добавлено в комплектацию"); return "redirect:/projects/" + projectId + "/configuration";
    }
    @GetMapping("/existing") @ResponseBody public java.util.Map<String,Object> existing(@PathVariable Long projectId,@RequestParam Long catalogItemId){return service.findExisting(projectId,catalogItemId).<java.util.Map<String,Object>>map(item->java.util.Map.of("exists",true,"url","/projects/"+projectId+"/items/"+item.getId()+"/edit?alreadyExists=1")).orElseGet(()->java.util.Map.of("exists",false));}
    @GetMapping("/{id}/edit") public String editForm(@PathVariable Long projectId, @PathVariable Long id, @RequestParam(required=false) String alreadyExists, Model model) { ProjectItem item = service.findByProjectAndId(projectId, id); item.getAllocations().forEach(a->{if(a.getProjectAssembly()==null)a.setProjectAssembly(new ProjectAssembly());}); if(alreadyExists!=null)model.addAttribute("successMessage","Изделие уже есть в комплектации — отредактируйте его распределение"); model.addAttribute("projectItem", item); formData(projectId, model, "Редактирование позиции"); return "project-items/form"; }
    @PostMapping("/{id}") public String update(@PathVariable Long projectId, @PathVariable Long id, @Valid @ModelAttribute ProjectItem projectItem, BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { projectItem.setId(id); formData(projectId, model, "Редактирование позиции"); return "project-items/form"; }
        try { service.update(projectId, id, projectItem); } catch (IllegalArgumentException exception) { result.reject("invalidReferences", exception.getMessage()); formData(projectId, model, "Редактирование позиции"); return "project-items/form"; }
        redirect.addFlashAttribute("successMessage", "Позиция обновлена"); return "redirect:/projects/" + projectId + "/configuration";
    }
    @PostMapping("/{id}/delete") public String delete(@PathVariable Long projectId, @PathVariable Long id, RedirectAttributes redirect) { try{java.math.BigDecimal removed=service.delete(projectId,id);redirect.addFlashAttribute("successMessage","Из комплектации удалено: "+removed.stripTrailingZeros().toPlainString());}catch(IllegalStateException exception){redirect.addFlashAttribute("errorMessage",exception.getMessage());}return "redirect:/projects/" + projectId + "/configuration"; }
    private void formData(Long projectId, Model model, String title) { model.addAttribute("project", projectService.findById(projectId)); model.addAttribute("assemblies", assemblyService.findByProject(projectId)); model.addAttribute("pageTitle", title);ProjectItem item=(ProjectItem)model.getAttribute("projectItem");model.addAttribute("hasAnalogs",item!=null&&item.getId()!=null&&item.getCatalogItem()!=null&&!analogs.analogs(item.getCatalogItem().getId()).isEmpty()); }
    private ProjectItemAllocation blankAllocation(){ProjectItemAllocation allocation=new ProjectItemAllocation();allocation.setProjectAssembly(new ProjectAssembly());return allocation;}
}
