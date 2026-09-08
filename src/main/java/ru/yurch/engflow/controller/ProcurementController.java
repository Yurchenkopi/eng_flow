package ru.yurch.engflow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yurch.engflow.model.*;
import ru.yurch.engflow.service.*;
import java.math.BigDecimal;
import java.util.stream.Collectors;

@Controller @RequestMapping("/projects/{projectId}/procurements")
public class ProcurementController {
    private final ProcurementService service;private final ProjectService projects;private final ProjectItemService projectItems;private final OrganizationService organizations;
    public ProcurementController(ProcurementService service,ProjectService projects,ProjectItemService projectItems,OrganizationService organizations){this.service=service;this.projects=projects;this.projectItems=projectItems;this.organizations=organizations;}
    @GetMapping public String list(@PathVariable Long projectId,Model model){var procurements=service.findByProject(projectId);model.addAttribute("project",projects.findById(projectId));model.addAttribute("procurements",procurements);model.addAttribute("lineCounts",procurements.stream().collect(Collectors.toMap(Procurement::getId,p->service.lineCount(p.getId()))));model.addAttribute("invoiceCounts",procurements.stream().collect(Collectors.toMap(Procurement::getId,p->service.invoiceCount(p.getId()))));model.addAttribute("statuses",procurements.stream().collect(Collectors.toMap(Procurement::getId,service::status)));return "procurements/list";}
    @GetMapping("/new") public String createForm(@PathVariable Long projectId,Model model){model.addAttribute("procurement",new Procurement());formData(projectId,"Новая закупка",model);return "procurements/form";}
    @PostMapping public String create(@PathVariable Long projectId,@ModelAttribute Procurement procurement,RedirectAttributes redirect,Model model){try{Procurement saved=service.create(projectId,procurement);redirect.addFlashAttribute("successMessage","Закупка создана");return redirect(projectId,saved.getId());}catch(RuntimeException exception){model.addAttribute("errorMessage",exception.getMessage());model.addAttribute("procurement",procurement);formData(projectId,"Новая закупка",model);return "procurements/form";}}
    @GetMapping("/{id}") public String details(@PathVariable Long projectId,@PathVariable Long id,Model model){Procurement procurement=service.find(projectId,id);var lines=service.lines(id);model.addAttribute("project",procurement.getProject());model.addAttribute("procurement",procurement);model.addAttribute("procurementLines",lines);model.addAttribute("invoices",service.invoices(id));model.addAttribute("status",service.status(procurement));model.addAttribute("newLine",new ProcurementLine());model.addAttribute("projectItems",projectItems.findByProject(projectId));model.addAttribute("requested",projectItems.findByProject(projectId).stream().collect(Collectors.toMap(ProjectItem::getId,item->service.progress(item).requestedQuantity())));return "procurements/details";}
    @GetMapping("/{id}/edit") public String editForm(@PathVariable Long projectId,@PathVariable Long id,Model model){model.addAttribute("procurement",service.find(projectId,id));formData(projectId,"Редактирование закупки",model);return "procurements/form";}
    @PostMapping("/{id}") public String update(@PathVariable Long projectId,@PathVariable Long id,@ModelAttribute Procurement procurement,RedirectAttributes redirect,Model model){try{service.update(projectId,id,procurement);redirect.addFlashAttribute("successMessage","Закупка обновлена");return redirect(projectId,id);}catch(RuntimeException exception){procurement.setId(id);model.addAttribute("errorMessage",exception.getMessage());model.addAttribute("procurement",procurement);formData(projectId,"Редактирование закупки",model);return "procurements/form";}}
    @PostMapping("/{id}/rfq-sent") public String markRfq(@PathVariable Long projectId,@PathVariable Long id,RedirectAttributes redirect){service.markRfqSent(projectId,id);redirect.addFlashAttribute("successMessage","Запрос отмечен как отправленный");return redirect(projectId,id);}
    @PostMapping("/{id}/lines") public String addLine(@PathVariable Long projectId,@PathVariable Long id,@ModelAttribute ProcurementLine line,RedirectAttributes redirect){try{service.addLine(projectId,id,line);redirect.addFlashAttribute("successMessage","Позиция добавлена в закупку");}catch(RuntimeException exception){redirect.addFlashAttribute("errorMessage",exception.getMessage());}return redirect(projectId,id);}
    private void formData(Long projectId,String title,Model model){model.addAttribute("project",projects.findById(projectId));model.addAttribute("suppliers",organizations.findSuppliers());model.addAttribute("pageTitle",title);}
    private String redirect(Long projectId,Long id){return "redirect:/projects/"+projectId+"/procurements/"+id;}
}
