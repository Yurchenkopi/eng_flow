package ru.yurch.engflow.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.model.*;
import ru.yurch.engflow.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
@Service @Transactional(readOnly=true)
public class TransferActService{
    private final TransferActRepository acts;private final TransferActItemRepository items;private final TransferActNumberSequenceRepository sequences;private final ProjectService projects;private final ProjectItemService projectItems;
    public TransferActService(TransferActRepository acts,TransferActItemRepository items,TransferActNumberSequenceRepository sequences,ProjectService projects,ProjectItemService projectItems){this.acts=acts;this.items=items;this.sequences=sequences;this.projects=projects;this.projectItems=projectItems;}
    public List<TransferAct> findAll(){return acts.findAllByOrderByYearDescNumberDesc();}
    public TransferAct findById(Long id){TransferAct act=acts.findById(id).orElseThrow(()->new IllegalArgumentException("Акт не найден: "+id));calculateTotals(act);return act;}
    public TransferAct prepare(Long projectId,List<Long> selectedIds){TransferAct act=new TransferAct();act.setProject(projects.findById(projectId));for(Long id:selectedIds){ProjectItem projectItem=projectItems.findByProjectAndId(projectId,id);TransferActItem line=new TransferActItem();line.setProjectItem(projectItem);line.setQuantity(projectItem.getRequiredQuantity().subtract(transferred(id)).max(BigDecimal.ZERO));act.getItems().add(line);}return act;}
    @Transactional public synchronized TransferAct create(TransferAct act){
        if(act.getProject()==null||act.getProject().getId()==null)throw new IllegalArgumentException("Не указан проект");Project project=projects.findById(act.getProject().getId());if(act.getItems()==null||act.getItems().isEmpty())throw new IllegalArgumentException("Выберите хотя бы одну позицию");
        act.setId(null);act.setProject(project);int year=act.getActDate().getYear();act.setYear(year);act.setNumber(nextNumber(year));
        Map<Long,BigDecimal> requested=new HashMap<>();for(TransferActItem line:act.getItems()){if(line.getProjectItem()==null||line.getProjectItem().getId()==null)throw new IllegalArgumentException("Не выбрана позиция акта");ProjectItem projectItem=projectItems.findByProjectAndId(project.getId(),line.getProjectItem().getId());line.setProjectItem(projectItem);line.setTransferAct(act);requested.merge(projectItem.getId(),line.getQuantity(),BigDecimal::add);}
        requested.forEach((id,quantity)->{ProjectItem item=projectItems.findByProjectAndId(project.getId(),id);BigDecimal available=item.getRequiredQuantity().subtract(transferred(id));if(quantity.compareTo(available)>0)throw new IllegalArgumentException("Передаваемое количество для «"+item.getCatalogItem().getName()+"» превышает остаток "+available.stripTrailingZeros().toPlainString());});
        return acts.save(act);
    }
    public BigDecimal transferred(Long projectItemId){BigDecimal value=items.transferred(projectItemId);return value==null?BigDecimal.ZERO:value;}
    public Map<Long,BigDecimal> transferredByProject(Long projectId){return items.transferredByProject(projectId).stream().collect(Collectors.toMap(row->(Long)row[0],row->(BigDecimal)row[1]));}
    private int nextNumber(int year){TransferActNumberSequence sequence=sequences.findForUpdate(year).orElseGet(()->{TransferActNumberSequence created=new TransferActNumberSequence();created.setYear(year);created.setNextNumber(1);return sequences.saveAndFlush(created);});int number=sequence.getNextNumber();sequence.setNextNumber(number+1);sequences.save(sequence);return number;}
    private void calculateTotals(TransferAct act){Map<Long,BigDecimal> totals=act.getItems().stream().collect(Collectors.groupingBy(line->line.getProjectItem().getCatalogItem().getId(),Collectors.reducing(BigDecimal.ZERO,TransferActItem::getQuantity,BigDecimal::add)));act.getItems().forEach(line->line.setTotalSameCatalogItem(totals.get(line.getProjectItem().getCatalogItem().getId())));}
}
