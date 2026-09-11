package ru.yurch.engflow.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yurch.engflow.model.SupplierInvoice;
import ru.yurch.engflow.model.SupplierInvoiceLine;
import ru.yurch.engflow.service.ProcurementService;
import ru.yurch.engflow.service.SupplierInvoiceService;

@Controller
@RequestMapping("/projects/{projectId}/procurements/{procurementId}/invoices")
public class SupplierInvoiceController {

    private final SupplierInvoiceService service;
    private final ProcurementService procurements;

    public SupplierInvoiceController(SupplierInvoiceService service, ProcurementService procurements) {
        this.service = service;
        this.procurements = procurements;
    }

    @GetMapping("/new")
    public String createForm(@PathVariable Long projectId, @PathVariable Long procurementId, Model model) {
        SupplierInvoice invoice = new SupplierInvoice();
        invoice.setInvoiceDate(LocalDate.now());
        model.addAttribute("invoice", invoice);
        formData(projectId, procurementId, "Новый счет", model);
        return "supplier-invoices/form";
    }

    @PostMapping
    public String create(
            @PathVariable Long projectId,
            @PathVariable Long procurementId,
            @ModelAttribute SupplierInvoice invoice,
            @RequestParam(required = false) MultipartFile file,
            Model model,
            RedirectAttributes redirect) {
        try {
            SupplierInvoice saved = service.create(projectId, procurementId, invoice, file);
            redirect.addFlashAttribute("successMessage", "Счет добавлен");
            return redirect(projectId, procurementId, saved.getId());
        } catch (RuntimeException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("invoice", invoice);
            formData(projectId, procurementId, "Новый счет", model);
            return "supplier-invoices/form";
        }
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long projectId, @PathVariable Long procurementId, @PathVariable Long id, Model model) {
        SupplierInvoice invoice = service.find(projectId, procurementId, id);
        model.addAttribute("project", invoice.getProcurement().getProject());
        model.addAttribute("procurement", invoice.getProcurement());
        model.addAttribute("invoice", invoice);
        model.addAttribute("invoiceLines", service.lines(id));
        model.addAttribute("procurementLines", procurements.lines(procurementId));
        model.addAttribute("newLine", new SupplierInvoiceLine());
        return "supplier-invoices/details";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long projectId, @PathVariable Long procurementId, @PathVariable Long id, Model model) {
        model.addAttribute("invoice", service.find(projectId, procurementId, id));
        formData(projectId, procurementId, "Редактирование счета", model);
        return "supplier-invoices/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long projectId,
            @PathVariable Long procurementId,
            @PathVariable Long id,
            @ModelAttribute SupplierInvoice invoice,
            @RequestParam(required = false) MultipartFile file,
            Model model,
            RedirectAttributes redirect) {
        try {
            service.update(projectId, procurementId, id, invoice, file);
            redirect.addFlashAttribute("successMessage", "Счет обновлен");
            return redirect(projectId, procurementId, id);
        } catch (RuntimeException exception) {
            invoice.setId(id);
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("invoice", invoice);
            formData(projectId, procurementId, "Редактирование счета", model);
            return "supplier-invoices/form";
        }
    }

    @PostMapping("/{id}/lines")
    public String addLine(
            @PathVariable Long projectId,
            @PathVariable Long procurementId,
            @PathVariable Long id,
            @ModelAttribute SupplierInvoiceLine line,
            RedirectAttributes redirect) {
        try {
            service.addLine(projectId, procurementId, id, line);
            redirect.addFlashAttribute("successMessage", "Позиция добавлена в счет");
        } catch (RuntimeException exception) {
            redirect.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return redirect(projectId, procurementId, id);
    }

    @PostMapping("/{id}/submit-payment")
    public String submit(
            @PathVariable Long projectId,
            @PathVariable Long procurementId,
            @PathVariable Long id,
            RedirectAttributes redirect) {
        service.submitForPayment(projectId, procurementId, id);
        redirect.addFlashAttribute("successMessage", "Счет передан в оплату");
        return redirect(projectId, procurementId, id);
    }

    @PostMapping("/{id}/file/delete")
    public String deleteFile(
            @PathVariable Long projectId,
            @PathVariable Long procurementId,
            @PathVariable Long id,
            RedirectAttributes redirect) {
        service.deleteFile(projectId, procurementId, id);
        redirect.addFlashAttribute("successMessage", "Файл счета удален");
        return redirect(projectId, procurementId, id);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> file(@PathVariable Long projectId, @PathVariable Long procurementId, @PathVariable Long id) {
        SupplierInvoice invoice = service.find(projectId, procurementId, id);
        Resource resource = service.file(projectId, procurementId, id);
        MediaType type = invoice.getFileContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(invoice.getFileContentType());
        return ResponseEntity.ok().contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(invoice.getFileOriginalName(), StandardCharsets.UTF_8).build().toString())
                .body(resource);
    }

    private void formData(Long projectId, Long procurementId, String title, Model model) {
        model.addAttribute("procurement", procurements.find(projectId, procurementId));
        model.addAttribute("project", procurements.find(projectId, procurementId).getProject());
        model.addAttribute("pageTitle", title);
    }

    private String redirect(Long projectId, Long procurementId, Long id) {
        return "redirect:/projects/" + projectId + "/procurements/" + procurementId + "/invoices/" + id;
    }
}
