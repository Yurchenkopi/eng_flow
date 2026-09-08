package ru.yurch.engflow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.model.*;
import ru.yurch.engflow.repository.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service @Transactional(readOnly=true)
public class ProcurementService {
    private final ProcurementRepository procurements;private final ProcurementLineRepository lines;private final SupplierInvoiceRepository invoices;private final SupplierInvoiceLineRepository invoiceLines;private final ProjectService projects;private final ProjectItemService projectItems;private final OrganizationService organizations;
    public ProcurementService(ProcurementRepository procurements,ProcurementLineRepository lines,SupplierInvoiceRepository invoices,SupplierInvoiceLineRepository invoiceLines,ProjectService projects,ProjectItemService projectItems,OrganizationService organizations){this.procurements=procurements;this.lines=lines;this.invoices=invoices;this.invoiceLines=invoiceLines;this.projects=projects;this.projectItems=projectItems;this.organizations=organizations;}
    public List<Procurement> findByProject(Long projectId){projects.findById(projectId);return procurements.findByProjectIdOrderByCreatedAtDesc(projectId);}
    public Procurement find(Long projectId,Long id){return procurements.findByIdAndProjectId(id,projectId).orElseThrow(()->new IllegalArgumentException("Закупка не найдена"));}
    public List<ProcurementLine> lines(Long procurementId){return lines.findByProcurementIdOrderByIdAsc(procurementId);}
    public List<SupplierInvoice> invoices(Long procurementId){return invoices.findByProcurementIdOrderByInvoiceDateDescIdDesc(procurementId);}
    public long lineCount(Long procurementId){return lines.countByProcurementId(procurementId);}
    public long invoiceCount(Long procurementId){return invoices.countByProcurementId(procurementId);}
    @Transactional public Procurement create(Long projectId,Procurement value){value.setId(null);value.setProject(projects.findById(projectId));value.setSupplier(requireSupplier(value.getSupplier()));value.setNotes(trim(value.getNotes()));return procurements.save(value);}
    @Transactional public Procurement update(Long projectId,Long id,Procurement value){Procurement current=find(projectId,id);current.setSupplier(requireSupplier(value.getSupplier()));current.setNotes(trim(value.getNotes()));return procurements.save(current);}
    @Transactional public void markRfqSent(Long projectId,Long id){find(projectId,id).setRfqSentAt(Instant.now());}
    @Transactional public ProcurementLine addLine(Long projectId,Long procurementId,ProcurementLine value){Procurement procurement=find(projectId,procurementId);if(value.getProjectItem()==null||value.getProjectItem().getId()==null)throw new IllegalArgumentException("Выберите позицию комплектации");ProjectItem item=projectItems.findByProjectAndId(projectId,value.getProjectItem().getId());positive(value.getRequestedQuantity(),"Запрашиваемое количество");if(lines.existsByProcurementIdAndProjectItemId(procurementId,item.getId()))throw new IllegalArgumentException("Позиция уже добавлена в эту закупку");BigDecimal remaining=item.getRequiredQuantity().subtract(lines.requestedByProjectItem(item.getId()));if(value.getRequestedQuantity().compareTo(remaining)>0)throw new IllegalArgumentException("Количество превышает остаток к запросу: "+remaining.stripTrailingZeros().toPlainString());value.setId(null);value.setProcurement(procurement);value.setProjectItem(item);value.setNotes(trim(value.getNotes()));return lines.save(value);}
    public ProcurementProgress progress(ProjectItem item){BigDecimal requested=lines.requestedByProjectItem(item.getId());BigDecimal ordered=invoiceLines.orderedByProjectItem(item.getId());ProcurementStatus status;if(ordered.signum()>0)status=ProcurementStatus.ORDER_PLACED;else if(hasSentRfq(item.getId()))status=ProcurementStatus.RFQ_SENT;else status=ProcurementStatus.NOT_REQUESTED;return new ProcurementProgress(status,requested,ordered,item.getRequiredQuantity());}
    public Map<Long,ProcurementProgress> progressFor(Collection<ProjectItem> items){Map<Long,ProcurementProgress> result=new LinkedHashMap<>();items.forEach(item->result.put(item.getId(),progress(item)));return result;}
    public ProcurementStatus status(Procurement value){if(invoiceLines.orderedByProcurement(value.getId()).signum()>0)return ProcurementStatus.ORDER_PLACED;return value.getRfqSentAt()!=null?ProcurementStatus.RFQ_SENT:ProcurementStatus.NOT_REQUESTED;}
    private boolean hasSentRfq(Long projectItemId){return lines.existsByProjectItemIdAndProcurementRfqSentAtIsNotNull(projectItemId);}
    private Organization requireSupplier(Organization reference){if(reference==null||reference.getId()==null)throw new IllegalArgumentException("Выберите поставщика");Organization supplier=organizations.findById(reference.getId());if(!supplier.getRoles().contains(OrganizationRole.SUPPLIER))throw new IllegalArgumentException("Организация не имеет роли поставщика");return supplier;}
    private void positive(BigDecimal value,String label){if(value==null||value.signum()<=0)throw new IllegalArgumentException(label+" должно быть положительным");}
    private String trim(String value){return value==null||value.isBlank()?null:value.trim();}
}
