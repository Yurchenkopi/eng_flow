package ru.yurch.engflow.controller;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yurch.engflow.model.Contact;
import ru.yurch.engflow.service.ContactService;
import ru.yurch.engflow.service.OrganizationService;
@Controller @RequestMapping("/organizations/{organizationId}/contacts")
public class ContactController{
    private final ContactService contacts;private final OrganizationService organizations;
    public ContactController(ContactService contacts,OrganizationService organizations){this.contacts=contacts;this.organizations=organizations;}
    @GetMapping("/new") public String createForm(@PathVariable Long organizationId,Model model){model.addAttribute("contact",new Contact());formData(organizationId,"Новый контакт",model);return "contacts/form";}
    @PostMapping public String create(@PathVariable Long organizationId,@Valid @ModelAttribute Contact contact,BindingResult result,Model model,RedirectAttributes redirect){if(result.hasErrors()){formData(organizationId,"Новый контакт",model);return "contacts/form";}contacts.create(organizationId,contact);redirect.addFlashAttribute("successMessage","Контакт добавлен");return redirect(organizationId);}
    @GetMapping("/{id}/edit") public String editForm(@PathVariable Long organizationId,@PathVariable Long id,Model model){model.addAttribute("contact",contacts.find(organizationId,id));formData(organizationId,"Редактирование контакта",model);return "contacts/form";}
    @PostMapping("/{id}") public String update(@PathVariable Long organizationId,@PathVariable Long id,@Valid @ModelAttribute Contact contact,BindingResult result,Model model,RedirectAttributes redirect){if(result.hasErrors()){contact.setId(id);formData(organizationId,"Редактирование контакта",model);return "contacts/form";}contacts.update(organizationId,id,contact);redirect.addFlashAttribute("successMessage","Контакт обновлен");return redirect(organizationId);}
    private void formData(Long id,String title,Model model){model.addAttribute("organization",organizations.findById(id));model.addAttribute("pageTitle",title);}
    private String redirect(Long id){return "redirect:/organizations/"+id+"/edit#contacts";}
}
