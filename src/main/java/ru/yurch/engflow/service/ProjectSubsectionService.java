package ru.yurch.engflow.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.model.*;
import ru.yurch.engflow.repository.ProjectSubsectionRepository;
import java.util.Objects;
@Service @Transactional(readOnly=true)
public class ProjectSubsectionService {
 private final ProjectSubsectionRepository repository; public ProjectSubsectionService(ProjectSubsectionRepository repository){this.repository=repository;}
 public ProjectSubsection findForAssembly(Long id,ProjectAssembly assembly){ProjectSubsection value=repository.findById(id).orElseThrow(()->new IllegalArgumentException("Подраздел не найден: "+id));if(!value.getProjectAssembly().getId().equals(assembly.getId()))throw new IllegalArgumentException("Подраздел должен принадлежать выбранному разделу");return value;}
 @Transactional public ProjectSubsection resolve(ProjectAssembly assembly,String designation,String appliesFor){
  String normalized=designation==null?null:designation.trim(); String use=appliesFor==null||appliesFor.isBlank()?null:appliesFor.trim();
  if(normalized==null||normalized.isEmpty())throw new IllegalArgumentException("Укажите подраздел для передачи в цех");
  return repository.findByProjectAssemblyIdAndDesignationIgnoreCase(assembly.getId(),normalized).map(existing->{if(!Objects.equals(existing.getAppliesFor(),use))throw new IllegalArgumentException("«Применяется для» должно соответствовать существующему подразделу");return existing;}).orElseGet(()->{ProjectSubsection value=new ProjectSubsection();value.setProjectAssembly(assembly);value.setDesignation(normalized);value.setAppliesFor(use);return repository.save(value);});
 }
}
