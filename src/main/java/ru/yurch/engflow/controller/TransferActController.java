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
public class TransferActController{
    private final TransferActService service;private final ProjectItemService projectItems;
    public TransferActController(TransferActService service,ProjectItemService projectItems){this.service=service;this.projectItems=projectItems;}
    @GetMapping public String list(Model model){model.addAttribute("acts",service.findAll());return "transfer-acts/list";}
    @GetMapping("/new") public String createForm(@RequestParam Long projectId,@RequestParam List<Long> itemIds,Model model){model.addAttribute("transferAct",service.prepare(projectId,itemIds));return "transfer-acts/form";}
    @PostMapping public String create(@Valid @ModelAttribute TransferAct transferAct,BindingResult result,Model model,RedirectAttributes redirect){
        if(result.hasErrors()){hydrate(transferAct);return "transfer-acts/form";}try{TransferAct saved=service.create(transferAct);redirect.addFlashAttribute("successMessage","Акт создан");return "redirect:/transfer-acts/"+saved.getId();}catch(IllegalArgumentException exception){result.reject("act",exception.getMessage());hydrate(transferAct);return "transfer-acts/form";}
    }
    @GetMapping("/{id}") public String details(@PathVariable Long id,Model model){model.addAttribute("act",service.findById(id));return "transfer-acts/details";}
    private void hydrate(TransferAct act){if(act.getProject()!=null&&act.getProject().getId()!=null)for(TransferActItem line:act.getItems())if(line.getProjectItem()!=null&&line.getProjectItem().getId()!=null)line.setProjectItem(projectItems.findByProjectAndId(act.getProject().getId(),line.getProjectItem().getId()));}
}
