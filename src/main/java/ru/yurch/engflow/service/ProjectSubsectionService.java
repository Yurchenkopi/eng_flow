package ru.yurch.engflow.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.model.*;
import ru.yurch.engflow.repository.ProjectSubsectionRepository;
@Service @Transactional(readOnly=true)
public class ProjectSubsectionService {
 private final ProjectSubsectionRepository repository; public ProjectSubsectionService(ProjectSubsectionRepository repository){this.repository=repository;}
 public ProjectSubsection findForAssembly(Long id,ProjectAssembly assembly){ProjectSubsection value=repository.findById(id).orElseThrow(()->new IllegalArgumentException("Подраздел не найден: "+id));if(!value.getProjectAssembly().getId().equals(assembly.getId()))throw new IllegalArgumentException("Подраздел должен принадлежать выбранному разделу");return value;}
 @Transactional public ProjectSubsection resolve(ProjectAssembly assembly,String designation){
  String normalized=designation==null?null:designation.trim();
  if(normalized==null||normalized.isEmpty())throw new IllegalArgumentException("Укажите подраздел для передачи в цех");
  return repository.findByProjectAssemblyIdAndDesignationIgnoreCase(assembly.getId(),normalized).orElseGet(()->{ProjectSubsection value=new ProjectSubsection();value.setProjectAssembly(assembly);value.setDesignation(normalized);return repository.save(value);});
 }
}
