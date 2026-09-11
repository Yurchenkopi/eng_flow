package ru.yurch.engflow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yurch.engflow.model.ItemSupplier;
import ru.yurch.engflow.service.ItemSupplierService;

@Controller
@RequestMapping("/catalog-items/{catalogItemId}/suppliers")
public class ItemSupplierController {

    private final ItemSupplierService service;

    public ItemSupplierController(ItemSupplierService service) {
        this.service = service;
    }

    @PostMapping
    public String create(@PathVariable Long catalogItemId, @ModelAttribute ItemSupplier itemSupplier, RedirectAttributes redirect) {
        try {
            service.create(catalogItemId, itemSupplier);
            redirect.addFlashAttribute("successMessage", "Поставщик добавлен");
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/catalog-items/" + catalogItemId + "/edit";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long catalogItemId, @PathVariable Long id, Model model) {
        model.addAttribute("itemSupplier", service.find(catalogItemId, id));
        return "catalog-items/supplier-form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long catalogItemId,
            @PathVariable Long id,
            @ModelAttribute ItemSupplier itemSupplier,
            RedirectAttributes redirect) {
        service.update(catalogItemId, id, itemSupplier);
        redirect.addFlashAttribute("successMessage", "Данные поставщика обновлены");
        return "redirect:/catalog-items/" + catalogItemId + "/edit";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long catalogItemId, @PathVariable Long id, RedirectAttributes redirect) {
        service.delete(catalogItemId, id);
        redirect.addFlashAttribute("successMessage", "Поставщик удален");
        return "redirect:/catalog-items/" + catalogItemId + "/edit";
    }
}
