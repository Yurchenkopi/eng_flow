package ru.yurch.engflow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.model.*;
import ru.yurch.engflow.repository.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service @Transactional(readOnly=true)
public class TransferActService {
    private final TransferActRepository acts; private final TransferActItemRepository items; private final TransferActNumberSequenceRepository sequences; private final ProjectService projects; private final ProjectItemAllocationRepository allocations;
    public TransferActService(TransferActRepository acts,TransferActItemRepository items,TransferActNumberSequenceRepository sequences,ProjectService projects,ProjectItemAllocationRepository allocations){this.acts=acts;this.items=items;this.sequences=sequences;this.projects=projects;this.allocations=allocations;}
    public List<TransferAct> findAll(){return acts.findAllByOrderByYearDescNumberDesc();}
    public TransferAct findById(Long id){TransferAct act=acts.findById(id).orElseThrow(()->new IllegalArgumentException("Акт не найден: "+id));calculateTotals(act);return act;}
    public TransferAct prepare(Long projectId,List<Long> allocationIds){if(allocationIds==null||allocationIds.isEmpty())throw new IllegalArgumentException("Выберите хотя бы один подраздел для создания акта.");TransferAct act=new TransferAct();act.setActDate(java.time.LocalDate.now());act.setProject(projects.findById(projectId));for(Long id:new LinkedHashSet<>(allocationIds)){ProjectItemAllocation allocation=findAllocation(projectId,id);if(allocation.getProjectSubsection()==null)throw new IllegalArgumentException("Для передачи в цех необходимо указать подраздел");BigDecimal remaining=remaining(allocation);if(remaining.signum()<=0)throw new IllegalArgumentException("Все требуемое количество для «"+allocation.getProjectItem().getCatalogItem().getName()+"» уже передано в цех");TransferActItem line=new TransferActItem();line.setProjectItemAllocation(allocation);line.setQuantity(remaining);line.setDestinationDesignation(allocation.getProjectSubsection().getDesignation());line.setAppliesFor(allocation.getProjectSubsection().getAppliesFor());act.getItems().add(line);}return act;}
    @Transactional public synchronized TransferAct create(TransferAct act){Project project=requireProject(act,false);act.setId(null);act.setProject(project);act.setTransferred(false);int year=act.getActDate().getYear();act.setYear(year);act.setNumber(nextNumber(year));attachAndValidate(act,project);return acts.save(act);}
    @Transactional public TransferAct update(Long id,TransferAct input){TransferAct act=findById(id);requireDraft(act);Project project=requireProject(input,true);if(input.getActDate()==null||input.getActDate().getYear()!=act.getYear())throw new IllegalArgumentException("Год даты акта нельзя изменить после присвоения номера");act.setActDate(input.getActDate());act.setDeliveredBy(input.getDeliveredBy());act.setReceivedBy(input.getReceivedBy());act.setNotes(input.getNotes());act.getItems().clear();for(TransferActItem source:input.getItems()){TransferActItem line=new TransferActItem();line.setProjectItemAllocation(source.getProjectItemAllocation());line.setQuantity(source.getQuantity());line.setDestinationDesignation(source.getDestinationDesignation());line.setAppliesFor(source.getAppliesFor());line.setShopNumber(source.getShopNumber());line.setNotes(source.getNotes());act.getItems().add(line);}attachAndValidate(act,project);return acts.save(act);}
    @Transactional public void finalizeAct(Long id){TransferAct act=findById(id);requireDraft(act);if(act.getItems().isEmpty())throw new IllegalArgumentException("Пустой акт нельзя отметить как переданный");attachAndValidate(act,act.getProject());act.setTransferred(true);}
    @Transactional public void delete(Long id){TransferAct act=findById(id);requireDraft(act);acts.delete(act);}
    @Transactional public void removeItem(Long actId,Long itemId){TransferAct act=findById(actId);requireDraft(act);if(!act.getItems().removeIf(line->Objects.equals(line.getId(),itemId)))throw new IllegalArgumentException("Позиция акта не найдена: "+itemId);}
    public BigDecimal transferred(Long allocationId){BigDecimal value=items.transferred(allocationId);return value==null?BigDecimal.ZERO:value;}
    public Map<Long,BigDecimal> transferredByProject(Long projectId){return items.transferredByProject(projectId).stream().collect(Collectors.toMap(row->(Long)row[0],row->(BigDecimal)row[1]));}
    public BigDecimal remaining(ProjectItemAllocation allocation){return allocation.getQuantity().subtract(transferred(allocation.getId())).max(BigDecimal.ZERO);}
    private Project requireProject(TransferAct act,boolean allowEmpty){if(act.getProject()==null||act.getProject().getId()==null)throw new IllegalArgumentException("Не указан проект");Project project=projects.findById(act.getProject().getId());if(!allowEmpty&&(act.getItems()==null||act.getItems().isEmpty()))throw new IllegalArgumentException("Выберите хотя бы одну позицию");return project;}
    private void attachAndValidate(TransferAct act,Project project){
        Map<Long,BigDecimal> requested=new HashMap<>();
        for(TransferActItem line:act.getItems()){
            if(line.getProjectItemAllocation()==null||line.getProjectItemAllocation().getId()==null)throw new IllegalArgumentException("Не выбран раздел акта");
            if(line.getQuantity()==null||line.getQuantity().signum()<=0)throw new IllegalArgumentException("Количество должно быть положительным");
            ProjectItemAllocation allocation=findAllocation(project.getId(),line.getProjectItemAllocation().getId());
            if(allocation.getProjectSubsection()==null)throw new IllegalArgumentException("Для передачи в цех необходимо указать подраздел");
            if(line.getQuantity().remainder(allocation.getProjectItem().getCatalogItem().getQuantityStep()).signum()!=0)throw new IllegalArgumentException("Количество должно соответствовать шагу единицы измерения");
            line.setProjectItemAllocation(allocation);line.setTransferAct(act);requested.merge(allocation.getId(),line.getQuantity(),BigDecimal::add);
        }
        requested.forEach((id,quantity)->{ProjectItemAllocation allocation=findAllocation(project.getId(),id);BigDecimal available=remaining(allocation);if(quantity.compareTo(available)>0)throw new IllegalArgumentException("Передаваемое количество для «"+allocation.getProjectItem().getCatalogItem().getName()+"» превышает остаток "+available.stripTrailingZeros().toPlainString());});
    }
    private ProjectItemAllocation findAllocation(Long projectId,Long id){return allocations.findByIdAndProjectItemProjectId(id,projectId).orElseThrow(()->new IllegalArgumentException("Распределение проекта не найдено: "+id));}
    private void requireDraft(TransferAct act){if(act.isTransferred())throw new IllegalStateException("Переданный акт нельзя изменить или удалить");}
    private int nextNumber(int year){TransferActNumberSequence sequence=sequences.findForUpdate(year).orElseGet(()->{TransferActNumberSequence created=new TransferActNumberSequence();created.setYear(year);created.setNextNumber(1);return sequences.saveAndFlush(created);});int number=sequence.getNextNumber();sequence.setNextNumber(number+1);sequences.save(sequence);return number;}
    private void calculateTotals(TransferAct act){Map<Long,BigDecimal> totals=act.getItems().stream().collect(Collectors.groupingBy(line->line.getProjectItemAllocation().getProjectItem().getCatalogItem().getId(),Collectors.reducing(BigDecimal.ZERO,TransferActItem::getQuantity,BigDecimal::add)));act.getItems().forEach(line->line.setTotalSameCatalogItem(totals.get(line.getProjectItemAllocation().getProjectItem().getCatalogItem().getId())));}
}
