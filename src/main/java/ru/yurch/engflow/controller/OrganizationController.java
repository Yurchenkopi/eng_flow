package ru.yurch.engflow.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yurch.engflow.model.Organization;
import ru.yurch.engflow.service.OrganizationService;

@Controller
@RequestMapping("/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("organizations", organizationService.findAll());
        return "organizations/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("organization", new Organization());
        model.addAttribute("pageTitle", "Новая организация");
        return "organizations/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute Organization organization,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Новая организация");
            return "organizations/form";
        }
        organizationService.create(organization);
        redirectAttributes.addFlashAttribute("successMessage", "Организация создана");
        return "redirect:/organizations";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("organization", organizationService.findById(id));
        model.addAttribute("pageTitle", "Редактирование организации");
        return "organizations/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Organization organization,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            organization.setId(id);
            model.addAttribute("pageTitle", "Редактирование организации");
            return "organizations/form";
        }
        organizationService.update(id, organization);
        redirectAttributes.addFlashAttribute("successMessage", "Организация обновлена");
        return "redirect:/organizations";
    }
}
