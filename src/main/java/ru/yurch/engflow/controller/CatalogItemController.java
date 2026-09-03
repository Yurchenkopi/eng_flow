package ru.yurch.engflow.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yurch.engflow.model.CatalogItem;
import ru.yurch.engflow.service.CatalogItemService;
import ru.yurch.engflow.service.ProjectItemService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller @RequestMapping("/catalog-items")
public class CatalogItemController {
    private final CatalogItemService service; private final ProjectItemService projectItemService;
    public CatalogItemController(CatalogItemService service, ProjectItemService projectItemService) { this.service = service; this.projectItemService = projectItemService; }

    @GetMapping public String list(@RequestParam(required = false) String query, @RequestParam(defaultValue = "name") String sort, @RequestParam(defaultValue = "asc") String direction, Model model) {
        List<CatalogItem> items = service.findAll(query, sort, direction); model.addAttribute("items", items);
        model.addAttribute("usageCounts", items.stream().collect(Collectors.toMap(CatalogItem::getId, item -> projectItemService.countProjectsUsingCatalogItem(item.getId()))));
        model.addAttribute("query", query); model.addAttribute("sort", sort); model.addAttribute("direction", direction); return "catalog-items/list";
    }
    @GetMapping("/search") @ResponseBody public List<Map<String, Object>> autocomplete(@RequestParam String query) {
        return service.autocomplete(query).stream().map(item -> Map.<String, Object>of("id", item.getId(), "designation", item.getDesignation() == null ? "" : item.getDesignation(), "name", item.getName(), "manufacturer", item.getManufacturer() == null ? "" : item.getManufacturer())).toList();
    }
    @GetMapping("/new") public String createForm(@RequestParam(required = false) Long projectId, Model model) { CatalogItem item = new CatalogItem(); model.addAttribute("catalogItem", item); formData(model, "Новое изделие", projectId); return "catalog-items/form"; }
    @PostMapping public String create(@Valid @ModelAttribute CatalogItem catalogItem, BindingResult result, @RequestParam(required = false) Long projectId, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { formData(model, "Новое изделие", projectId); return "catalog-items/form"; }
        CatalogItem saved = service.create(catalogItem); redirect.addFlashAttribute("successMessage", "Изделие создано");
        return projectId == null ? "redirect:/catalog-items" : "redirect:/projects/" + projectId + "/items/new?catalogItemId=" + saved.getId();
    }
    @GetMapping("/{id}/edit") public String editForm(@PathVariable Long id, Model model) { model.addAttribute("catalogItem", service.findById(id)); formData(model, "Редактирование изделия", null); return "catalog-items/form"; }
    @GetMapping("/{id}/copy") public String copyForm(@PathVariable Long id, Model model) { model.addAttribute("catalogItem", service.prepareCopy(id)); formData(model, "Копия изделия", null); return "catalog-items/form"; }
    @PostMapping("/{id}") public String update(@PathVariable Long id, @Valid @ModelAttribute CatalogItem catalogItem, BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { catalogItem.setId(id); formData(model, "Редактирование изделия", null); return "catalog-items/form"; }
        service.update(id, catalogItem); redirect.addFlashAttribute("successMessage", "Изделие обновлено"); return "redirect:/catalog-items";
    }
    private void formData(Model model, String title, Long projectId) { model.addAttribute("pageTitle", title); model.addAttribute("projectId", projectId); }
}
