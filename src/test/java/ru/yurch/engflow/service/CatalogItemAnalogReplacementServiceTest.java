package ru.yurch.engflow.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.dto.AnalogReplacementForm;
import ru.yurch.engflow.dto.AnalogReplacementRow;
import ru.yurch.engflow.model.*;
import ru.yurch.engflow.repository.*;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.NONE) @ActiveProfiles("test") @Transactional
class CatalogItemAnalogReplacementServiceTest {
    @Autowired CatalogItemAnalogService analogs; @Autowired ProjectItemAnalogReplacementService replacements;
    @Autowired CatalogItemService catalogItems; @Autowired ProjectItemService projectItems;
    @Autowired ProjectRepository projects; @Autowired ProjectAssemblyRepository assemblies; @Autowired MeasurementUnitRepository units; @Autowired ProcurementService procurements; @Autowired OrganizationRepository organizations;

    @Test void relationIsSymmetricAndRejectsSelfAndDuplicate(){CatalogItem a=item("A"),b=item("B");analogs.add(a.getId(),b.getId());assertThat(analogs.analogs(a.getId())).extracting(CatalogItem::getId).containsExactly(b.getId());assertThat(analogs.analogs(b.getId())).extracting(CatalogItem::getId).containsExactly(a.getId());assertThatThrownBy(()->analogs.add(a.getId(),a.getId())).hasMessageContaining("самому себе");assertThatThrownBy(()->analogs.add(b.getId(),a.getId())).hasMessageContaining("уже существует");}

    @Test void replacementPreservesEveryAllocationAndMergesExistingTarget(){CatalogItem sourceCatalog=item("Исходное"),targetCatalog=item("Аналог");analogs.add(sourceCatalog.getId(),targetCatalog.getId());Project project=project();ProjectAssembly first=assembly(project,"Вакуум"),second=assembly(project,"Газ");ProjectItem source=projectItem(project,sourceCatalog,first,"Деталь 1","7");source.getAllocations().add(allocation(second,"Деталь 2","3"));source=projectItems.create(project.getId(),source);ProjectItem target=projectItem(project,targetCatalog,first,"Деталь 1","3");target=projectItems.create(project.getId(),target);
        AnalogReplacementForm form=new AnalogReplacementForm();form.setTargetCatalogItemId(targetCatalog.getId());form.getRows().add(row(source.getAllocations().get(0),"3","4"));form.getRows().add(row(source.getAllocations().get(1),"1","2"));replacements.replace(project.getId(),source.getId(),form);
        ProjectItem updatedSource=projectItems.findByProjectAndId(project.getId(),source.getId()),updatedTarget=projectItems.findByProjectAndId(project.getId(),target.getId());assertThat(updatedSource.getRequiredQuantity()).isEqualByComparingTo("4");assertThat(updatedTarget.getRequiredQuantity()).isEqualByComparingTo("9");assertThat(updatedTarget.getAllocations()).hasSize(2);assertThat(updatedTarget.getAllocations()).extracting(ProjectItemAllocation::getQuantity).containsExactlyInAnyOrder(new BigDecimal("7"),new BigDecimal("2"));assertThat(updatedTarget.getAllocations()).extracting(ProjectItemAllocation::getProjectAssembly).extracting(ProjectAssembly::getName).containsExactlyInAnyOrder("Вакуум","Газ");}

    @Test void replacementRejectsChangedAllocationTotal(){CatalogItem a=item("A2"),b=item("B2");analogs.add(a.getId(),b.getId());Project project=project();ProjectItem source=projectItems.create(project.getId(),projectItem(project,a,null,null,"10"));AnalogReplacementForm form=new AnalogReplacementForm();form.setTargetCatalogItemId(b.getId());form.getRows().add(row(source.getAllocations().getFirst(),"4","7"));assertThatThrownBy(()->replacements.replace(project.getId(),source.getId(),form)).hasMessageContaining("равняться исходному");}

    @Test void sentRequestsLimitReplaceableTotalAcrossSuppliers(){CatalogItem a=item("A3"),b=item("B3");analogs.add(a.getId(),b.getId());Project project=project();ProjectItem source=projectItems.create(project.getId(),projectItem(project,a,null,null,"10"));request(project,source,"A",new BigDecimal("2"));request(project,source,"B",new BigDecimal("3"));assertThat(replacements.lockedQuantity(project.getId(),source.getId())).isEqualByComparingTo("5");assertThat(replacements.replaceableQuantity(project.getId(),source.getId())).isEqualByComparingTo("5");AnalogReplacementForm form=new AnalogReplacementForm();form.setTargetCatalogItemId(b.getId());form.getRows().add(row(source.getAllocations().getFirst(),"4","6"));assertThatThrownBy(()->replacements.replace(project.getId(),source.getId(),form)).hasMessageContaining("Доступно для замены: 5");}

    private CatalogItem item(String name){CatalogItem value=new CatalogItem();value.setName(name);value.setMeasurementUnit(units.findByName("шт.").orElseThrow());return catalogItems.create(value);}
    private Project project(){Project value=new Project();value.setDesignation("ИТ"+System.nanoTime());value.setName("Проект");return projects.save(value);}
    private ProjectAssembly assembly(Project project,String name){ProjectAssembly value=new ProjectAssembly();value.setProject(project);value.setName(name);return assemblies.save(value);}
    private ProjectItem projectItem(Project project,CatalogItem catalog,ProjectAssembly assembly,String appliesFor,String quantity){ProjectItem value=new ProjectItem();value.setProject(project);value.setCatalogItem(catalog);value.getAllocations().add(allocation(assembly,appliesFor,quantity));return value;}
    private ProjectItemAllocation allocation(ProjectAssembly assembly,String appliesFor,String quantity){ProjectItemAllocation value=new ProjectItemAllocation();value.setProjectAssembly(assembly);value.setAppliesFor(appliesFor);value.setQuantity(new BigDecimal(quantity));return value;}
    private AnalogReplacementRow row(ProjectItemAllocation allocation,String original,String replacement){AnalogReplacementRow value=new AnalogReplacementRow();value.setSourceAllocationId(allocation.getId());value.setOriginalQuantity(new BigDecimal(original));value.setReplacementQuantity(new BigDecimal(replacement));return value;}
    private void request(Project project,ProjectItem item,String name,BigDecimal quantity){Organization supplier=new Organization();supplier.setName(name);supplier.getRoles().add(OrganizationRole.SUPPLIER);supplier=organizations.save(supplier);Procurement procurement=new Procurement();procurement.setSupplier(supplier);procurement=procurements.create(project.getId(),procurement);ProcurementLine line=new ProcurementLine();line.setProjectItem(item);line.setRequestedQuantity(quantity);procurements.addLine(project.getId(),procurement.getId(),line);procurements.markRfqSent(project.getId(),procurement.getId());}
}
