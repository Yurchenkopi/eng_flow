package ru.yurch.engflow.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yurch.engflow.model.*;
import ru.yurch.engflow.service.*;
import java.util.List;

@Controller @RequestMapping("/transfer-acts")
public class TransferActController {
    private final TransferActService service;private final ProjectItemService projectItems;
    public TransferActController(TransferActService service,ProjectItemService projectItems){this.service=service;this.projectItems=projectItems;}
    @GetMapping public String list(Model model){model.addAttribute("acts",service.findAll());return "transfer-acts/list";}
    @GetMapping("/new") public String createForm(@RequestParam Long projectId,@RequestParam(required=false) List<Long> itemIds,Model model,RedirectAttributes redirect){try{TransferAct act=service.prepare(projectId,itemIds);model.addAttribute("transferAct",act);formData(act,false,model);return "transfer-acts/form";}catch(IllegalArgumentException exception){redirect.addFlashAttribute("errorMessage",exception.getMessage());return "redirect:/projects/"+projectId+"/configuration";}}
    @PostMapping public String create(@Valid @ModelAttribute TransferAct transferAct,BindingResult result,Model model,RedirectAttributes redirect){if(result.hasErrors())return invalidForm(transferAct,false,model);try{TransferAct saved=service.create(transferAct);redirect.addFlashAttribute("successMessage","Акт создан как черновик");return "redirect:/transfer-acts/"+saved.getId();}catch(RuntimeException exception){result.reject("act",exception.getMessage());return invalidForm(transferAct,false,model);}}
    @GetMapping("/{id}") public String details(@PathVariable Long id,Model model){model.addAttribute("act",service.findById(id));return "transfer-acts/details";}
    @GetMapping("/{id}/edit") public String editForm(@PathVariable Long id,Model model,RedirectAttributes redirect){TransferAct act=service.findById(id);if(act.isTransferred()){redirect.addFlashAttribute("errorMessage","Переданный акт нельзя редактировать");return "redirect:/transfer-acts/"+id;}model.addAttribute("transferAct",act);formData(act,true,model);return "transfer-acts/form";}
    @PostMapping("/{id}") public String update(@PathVariable Long id,@Valid @ModelAttribute TransferAct transferAct,BindingResult result,Model model,RedirectAttributes redirect){if(result.hasErrors())return invalidForm(transferAct,true,model);try{service.update(id,transferAct);redirect.addFlashAttribute("successMessage","Акт обновлен");return "redirect:/transfer-acts/"+id;}catch(RuntimeException exception){result.reject("act",exception.getMessage());return invalidForm(transferAct,true,model);}}
    @PostMapping("/{id}/finalize") public String finalizeAct(@PathVariable Long id,RedirectAttributes redirect){return actAction(id,redirect,()->service.finalizeAct(id),"Акт отмечен как переданный");}
    @PostMapping("/{id}/delete") public String delete(@PathVariable Long id,RedirectAttributes redirect){try{service.delete(id);redirect.addFlashAttribute("successMessage","Акт удален");return "redirect:/transfer-acts";}catch(RuntimeException exception){redirect.addFlashAttribute("errorMessage",exception.getMessage());return "redirect:/transfer-acts/"+id;}}
    @PostMapping("/{id}/items/{itemId}/delete") public String removeItem(@PathVariable Long id,@PathVariable Long itemId,RedirectAttributes redirect){return actAction(id,redirect,()->service.removeItem(id,itemId),"Позиция удалена из акта");}
    private String actAction(Long id,RedirectAttributes redirect,Runnable action,String message){try{action.run();redirect.addFlashAttribute("successMessage",message);}catch(RuntimeException exception){redirect.addFlashAttribute("errorMessage",exception.getMessage());}return "redirect:/transfer-acts/"+id;}
    private String invalidForm(TransferAct act,boolean editing,Model model){hydrate(act);formData(act,editing,model);return "transfer-acts/form";}
    private void formData(TransferAct act,boolean editing,Model model){model.addAttribute("editing",editing);model.addAttribute("available",act.getItems().stream().collect(java.util.stream.Collectors.toMap(line->line.getProjectItem().getId(),line->service.remaining(line.getProjectItem()),(left,right)->left)));}
    private void hydrate(TransferAct act){if(act.getProject()!=null&&act.getProject().getId()!=null)for(TransferActItem line:act.getItems())if(line.getProjectItem()!=null&&line.getProjectItem().getId()!=null)line.setProjectItem(projectItems.findByProjectAndId(act.getProject().getId(),line.getProjectItem().getId()));}
}
