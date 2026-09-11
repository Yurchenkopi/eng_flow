package ru.yurch.engflow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yurch.engflow.service.CatalogItemAnalogService;

@Controller
@RequestMapping("/catalog-items/{itemId}/analogs")
public class CatalogItemAnalogController {

    private final CatalogItemAnalogService service;

    public CatalogItemAnalogController(CatalogItemAnalogService service) {
        this.service = service;
    }

    @PostMapping
    public String add(@PathVariable Long itemId, @RequestParam Long analogId, RedirectAttributes redirect) {
        try {
            service.add(itemId, analogId);
            redirect.addFlashAttribute("successMessage", "Аналог добавлен");
        } catch (RuntimeException exception) {
            redirect.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return back(itemId);
    }

    @PostMapping("/{relationId}/delete")
    public String remove(@PathVariable Long itemId, @PathVariable Long relationId, RedirectAttributes redirect) {
        try {
            service.remove(itemId, relationId);
            redirect.addFlashAttribute("successMessage", "Связь с аналогом удалена");
        } catch (RuntimeException exception) {
            redirect.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return back(itemId);
    }

    private String back(Long itemId) {
        return "redirect:/catalog-items/" + itemId + "/edit";
    }
}
