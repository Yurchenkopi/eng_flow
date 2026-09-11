package ru.yurch.engflow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yurch.engflow.dto.AnalogReplacementForm;
import ru.yurch.engflow.service.*;

@Controller
@RequestMapping("/projects/{projectId}/items/{itemId}/analog-replacement")
public class ProjectItemAnalogController {
    private final ProjectItemService projectItems;private final ProjectItemAnalogReplacementService replacements;private final CatalogItemAnalogService analogs;
    public ProjectItemAnalogController(ProjectItemService projectItems,ProjectItemAnalogReplacementService replacements,CatalogItemAnalogService analogs){this.projectItems=projectItems;this.replacements=replacements;this.analogs=analogs;}
    @GetMapping public String form(@PathVariable Long projectId,@PathVariable Long itemId,Model model){var item=projectItems.findByProjectAndId(projectId,itemId);formData(projectId,itemId,item,model);model.addAttribute("replacement",replacements.prepare(projectId,itemId));return "project-items/analog-replacement";}
    @PostMapping public String replace(@PathVariable Long projectId,@PathVariable Long itemId,@ModelAttribute("replacement") AnalogReplacementForm form,Model model,RedirectAttributes redirect){try{replacements.replace(projectId,itemId,form);redirect.addFlashAttribute("successMessage","Количество перераспределено между оригиналом и аналогом");return "redirect:/projects/"+projectId+"/configuration";}catch(RuntimeException exception){var item=projectItems.findByProjectAndId(projectId,itemId);formData(projectId,itemId,item,model);model.addAttribute("errorMessage",exception.getMessage());return "project-items/analog-replacement";}}
    private void formData(Long projectId,Long itemId,ru.yurch.engflow.model.ProjectItem item,Model model){var locked=replacements.lockedQuantity(projectId,itemId);var replaceable=replacements.replaceableQuantity(projectId,itemId);model.addAttribute("projectItem",item);model.addAttribute("project",item.getProject());model.addAttribute("analogs",analogs.analogs(item.getCatalogItem().getId()));model.addAttribute("lockedQuantity",locked);model.addAttribute("replaceableQuantity",replaceable);if(locked.signum()>0&&!model.containsAttribute("errorMessage"))model.addAttribute("errorMessage",locked.stripTrailingZeros().toPlainString()+" из "+item.getRequiredQuantity().stripTrailingZeros().toPlainString()+" "+item.getCatalogItem().getUnit()+" уже запрошены. Можно заменить не более "+replaceable.stripTrailingZeros().toPlainString()+" "+item.getCatalogItem().getUnit()+".");}
}
