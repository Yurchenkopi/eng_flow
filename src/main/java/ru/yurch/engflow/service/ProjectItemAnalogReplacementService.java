package ru.yurch.engflow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.dto.AnalogReplacementForm;
import ru.yurch.engflow.dto.AnalogReplacementRow;
import ru.yurch.engflow.model.*;
import ru.yurch.engflow.repository.*;
import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional(readOnly=true)
public class ProjectItemAnalogReplacementService {
    private final ProjectItemService projectItems;
    private final ProjectItemRepository repository;
    private final CatalogItemService catalogItems;
    private final CatalogItemAnalogService analogs;
    private final TransferActItemRepository transferActItems;
    private final ProcurementLineRepository procurementLines;

    public ProjectItemAnalogReplacementService(ProjectItemService projectItems,ProjectItemRepository repository,CatalogItemService catalogItems,CatalogItemAnalogService analogs,TransferActItemRepository transferActItems,ProcurementLineRepository procurementLines){this.projectItems=projectItems;this.repository=repository;this.catalogItems=catalogItems;this.analogs=analogs;this.transferActItems=transferActItems;this.procurementLines=procurementLines;}

    public AnalogReplacementForm prepare(Long projectId,Long projectItemId){ProjectItem source=projectItems.findByProjectAndId(projectId,projectItemId);AnalogReplacementForm form=new AnalogReplacementForm();for(ProjectItemAllocation allocation:source.getAllocations()){AnalogReplacementRow row=new AnalogReplacementRow();row.setSourceAllocationId(allocation.getId());row.setOriginalQuantity(allocation.getQuantity());row.setReplacementQuantity(BigDecimal.ZERO);form.getRows().add(row);}return form;}
    public BigDecimal lockedQuantity(Long projectId,Long projectItemId){ProjectItem source=projectItems.findByProjectAndId(projectId,projectItemId);return procurementLines.requestedSentByProjectItem(source.getId()).min(source.getRequiredQuantity());}
    public BigDecimal replaceableQuantity(Long projectId,Long projectItemId){ProjectItem source=projectItems.findByProjectAndId(projectId,projectItemId);return source.getRequiredQuantity().subtract(lockedQuantity(projectId,projectItemId)).max(BigDecimal.ZERO);}

    @Transactional
    public ProjectItem replace(Long projectId,Long projectItemId,AnalogReplacementForm form){
        ProjectItem source=projectItems.findByProjectAndId(projectId,projectItemId);
        if(form.getTargetCatalogItemId()==null)throw new IllegalArgumentException("Выберите аналог");
        if(!analogs.areAnalogs(source.getCatalogItem().getId(),form.getTargetCatalogItemId()))throw new IllegalArgumentException("Выбранное изделие не является аналогом исходного");
        CatalogItem targetCatalog=catalogItems.findById(form.getTargetCatalogItemId());
        Map<Long,AnalogReplacementRow> rows=new HashMap<>();for(AnalogReplacementRow row:form.getRows())if(row.getSourceAllocationId()!=null&&rows.put(row.getSourceAllocationId(),row)!=null)throw new IllegalArgumentException("Распределение указано повторно");
        if(rows.size()!=source.getAllocations().size())throw new IllegalArgumentException("Для каждой исходной строки укажите распределение");
        BigDecimal replacementTotal=BigDecimal.ZERO;
        for(ProjectItemAllocation allocation:source.getAllocations()){
            AnalogReplacementRow row=rows.get(allocation.getId());if(row==null)throw new IllegalArgumentException("Не найдено распределение исходной позиции");
            nonNegative(row.getOriginalQuantity());nonNegative(row.getReplacementQuantity());
            if(row.getOriginalQuantity().add(row.getReplacementQuantity()).compareTo(allocation.getQuantity())!=0)throw new IllegalArgumentException("Сумма оригинала и аналога должна равняться исходному количеству для каждой строки");
            validateStep(row.getOriginalQuantity(),source.getCatalogItem());validateStep(row.getReplacementQuantity(),targetCatalog);replacementTotal=replacementTotal.add(row.getReplacementQuantity());
        }
        if(replacementTotal.signum()==0)throw new IllegalArgumentException("Укажите количество для замены");BigDecimal replaceable=source.getRequiredQuantity().subtract(procurementLines.requestedSentByProjectItem(source.getId())).max(BigDecimal.ZERO);if(replacementTotal.compareTo(replaceable)>0)throw new IllegalArgumentException("Уже запрошенное количество нельзя заменить. Доступно для замены: "+replaceable.stripTrailingZeros().toPlainString()+" "+source.getCatalogItem().getUnit());
        ProjectItem target=projectItems.findExisting(projectId,targetCatalog.getId()).map(existing->projectItems.findByProjectAndId(projectId,existing.getId())).orElseGet(()->{ProjectItem item=new ProjectItem();item.setProject(source.getProject());item.setCatalogItem(targetCatalog);return item;});
        List<ProjectItemAllocation> sourceSnapshot=new ArrayList<>(source.getAllocations());
        for(ProjectItemAllocation sourceAllocation:sourceSnapshot){AnalogReplacementRow row=rows.get(sourceAllocation.getId());if(row.getReplacementQuantity().signum()>0)mergeAllocation(target,sourceAllocation,row.getReplacementQuantity());if(row.getOriginalQuantity().signum()==0){if(transferActItems.existsByProjectItemAllocationId(sourceAllocation.getId()))throw new IllegalStateException("Нельзя полностью заменить распределение, которое уже используется в акте");source.getAllocations().remove(sourceAllocation);}else sourceAllocation.setQuantity(row.getOriginalQuantity());}
        if(source.getAllocations().isEmpty()){if(procurementLines.existsByProjectItemId(source.getId()))throw new IllegalStateException("Нельзя полностью заменить позицию, которая уже используется в закупке");repository.delete(source);}else repository.save(source);
        return repository.save(target);
    }

    private void mergeAllocation(ProjectItem target,ProjectItemAllocation source,BigDecimal quantity){ProjectItemAllocation existing=target.getAllocations().stream().filter(candidate->sameIdentity(candidate,source)).findFirst().orElse(null);if(existing!=null){existing.setQuantity(existing.getQuantity().add(quantity));return;}ProjectItemAllocation copy=new ProjectItemAllocation();copy.setProjectItem(target);copy.setProjectAssembly(source.getProjectAssembly());copy.setProjectSubsection(source.getProjectSubsection());copy.setAppliesFor(source.getAppliesFor());copy.setQuantity(quantity);copy.setNotes(source.getNotes());target.getAllocations().add(copy);}
    private boolean sameIdentity(ProjectItemAllocation left,ProjectItemAllocation right){return Objects.equals(id(left.getProjectAssembly()),id(right.getProjectAssembly()))&&Objects.equals(id(left.getProjectSubsection()),id(right.getProjectSubsection()))&&Objects.equals(normalize(left.getAppliesFor()),normalize(right.getAppliesFor()));}
    private Long id(Object value){if(value instanceof ProjectAssembly assembly)return assembly.getId();if(value instanceof ProjectSubsection subsection)return subsection.getId();return null;}
    private String normalize(String value){return value==null||value.isBlank()?null:value.trim();}
    private void nonNegative(BigDecimal value){if(value==null||value.signum()<0)throw new IllegalArgumentException("Количество не может быть отрицательным");}
    private void validateStep(BigDecimal value,CatalogItem item){if(value.signum()>0&&value.remainder(item.getQuantityStep()).signum()!=0)throw new IllegalArgumentException("Количество должно соответствовать шагу единицы измерения "+item.getUnit());}
}
