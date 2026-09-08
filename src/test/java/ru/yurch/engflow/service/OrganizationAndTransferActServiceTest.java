package ru.yurch.engflow.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.model.*;
import ru.yurch.engflow.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.NONE) @ActiveProfiles("test") @Transactional
class OrganizationAndTransferActServiceTest{
    @Autowired OrganizationService organizations;@Autowired ContactService contacts;@Autowired ProjectRepository projects;@Autowired CatalogItemRepository catalogItems;@Autowired ProjectItemRepository projectItems;@Autowired ProjectItemService projectItemService;@Autowired ProjectAssemblyRepository assemblies;@Autowired ProjectSubsectionRepository subsections;@Autowired MeasurementUnitRepository units;@Autowired TransferActService acts;

    @Test void storesRolesContactsAndFiltersCustomers(){
        Organization customer=organization("Заказчик",Set.of(OrganizationRole.CUSTOMER));Organization supplier=organization("Поставщик",Set.of(OrganizationRole.SUPPLIER));
        customer=organizations.create(customer);supplier=organizations.create(supplier);Contact contact=new Contact();contact.setFullName("Иванов Иван");contact.setPrimary(true);contact=contacts.create(customer.getId(),contact);
        assertThat(organizations.findCustomers()).extracting(Organization::getId).contains(customer.getId()).doesNotContain(supplier.getId());
        assertThat(contact.getOrganization().getId()).isEqualTo(customer.getId());assertThat(contact.isPrimary()).isTrue();
    }

    @Test void numbersActsByYearAndResetsForNewYear(){
        ProjectItem item=projectItem("ИТ801.00.00.000","Клапан",new BigDecimal("10"));
        TransferAct first=acts.create(act(item,LocalDate.of(2091,1,10),new BigDecimal("1")));
        TransferAct second=acts.create(act(item,LocalDate.of(2091,2,10),new BigDecimal("1")));
        TransferAct nextYear=acts.create(act(item,LocalDate.of(2092,1,10),new BigDecimal("1")));
        assertThat(first.getNumber()).isEqualTo(1);assertThat(second.getNumber()).isEqualTo(2);assertThat(nextYear.getNumber()).isEqualTo(1);
    }

    @Test void draftDoesNotCountUntilFinalizedAndTotalsAreCalculated(){
        ProjectItem item=projectItem("ИТ802.00.00.000","Труба",new BigDecimal("10"));TransferAct act=baseAct(item,LocalDate.of(2093,1,1));
        act.getItems().add(line(item,"2"));act.getItems().add(line(item,"3"));TransferAct saved=acts.create(act);TransferAct loaded=acts.findById(saved.getId());
        assertThat(saved.isTransferred()).isFalse();assertThat(acts.transferred(allocation(item).getId())).isEqualByComparingTo("0");assertThat(acts.remaining(allocation(item))).isEqualByComparingTo("10");
        assertThat(loaded.getItems()).hasSize(2).allSatisfy(line->assertThat(line.getTotalSameCatalogItem()).isEqualByComparingTo("5"));
        acts.finalizeAct(saved.getId());assertThat(acts.transferred(allocation(item).getId())).isEqualByComparingTo("5");assertThat(acts.remaining(allocation(item))).isEqualByComparingTo("5");
    }

    @Test void partiallyAndFullyTransferredItemsHaveCorrectRemaining(){ProjectItem item=projectItem("ИТ804.00.00.000","Муфта",new BigDecimal("5"));TransferAct first=acts.create(act(item,LocalDate.of(2095,1,1),new BigDecimal("3")));acts.finalizeAct(first.getId());assertThat(acts.remaining(allocation(item))).isEqualByComparingTo("2");TransferAct second=acts.create(act(item,LocalDate.of(2095,1,2),new BigDecimal("2")));acts.finalizeAct(second.getId());assertThat(acts.remaining(allocation(item))).isZero();assertThatThrownBy(()->acts.prepare(item.getProject().getId(),List.of(allocation(item).getId()))).hasMessageContaining("уже передано");}

    @Test void emptyActCannotFinalizeAndDraftItemCanBeRemoved(){ProjectItem item=projectItem("ИТ805.00.00.000","Штуцер",new BigDecimal("2"));TransferAct saved=acts.create(act(item,LocalDate.of(2096,1,1),new BigDecimal("1")));Long lineId=acts.findById(saved.getId()).getItems().getFirst().getId();acts.removeItem(saved.getId(),lineId);assertThat(acts.findById(saved.getId()).getItems()).isEmpty();assertThatThrownBy(()->acts.finalizeAct(saved.getId())).hasMessageContaining("Пустой акт");}

    @Test void transferredActIsImmutable(){ProjectItem item=projectItem("ИТ806.00.00.000","Фланец",new BigDecimal("2"));TransferAct saved=acts.create(act(item,LocalDate.of(2097,1,1),new BigDecimal("1")));Long lineId=acts.findById(saved.getId()).getItems().getFirst().getId();acts.finalizeAct(saved.getId());assertThatThrownBy(()->acts.update(saved.getId(),saved)).isInstanceOf(IllegalStateException.class);assertThatThrownBy(()->acts.delete(saved.getId())).isInstanceOf(IllegalStateException.class);assertThatThrownBy(()->acts.removeItem(saved.getId(),lineId)).isInstanceOf(IllegalStateException.class);}
    @Test void projectItemUsedByTransferredActCannotBeDeleted(){ProjectItem item=projectItem("ИТ816.00.00.000","Фланец",BigDecimal.ONE);TransferAct act=acts.create(act(item,LocalDate.of(2097,2,1),BigDecimal.ONE));acts.finalizeAct(act.getId());assertThatThrownBy(()->projectItemService.delete(item.getProject().getId(),item.getId())).hasMessageContaining("используется в акте");assertThat(acts.findById(act.getId()).getItems()).hasSize(1);}

    @Test void draftDoesNotReduceAvailableQuantity(){ProjectItem item=projectItem("ИТ807.00.00.000","Кольцо",new BigDecimal("4"));acts.create(act(item,LocalDate.of(2098,1,1),new BigDecimal("3")));assertThat(acts.remaining(allocation(item))).isEqualByComparingTo("4");assertThat(acts.prepare(item.getProject().getId(),List.of(allocation(item).getId())).getItems().getFirst().getQuantity()).isEqualByComparingTo("4");}

    @Test void rejectsTransferAboveRequiredQuantity(){
        ProjectItem item=projectItem("ИТ803.00.00.000","Датчик",new BigDecimal("2"));
        assertThatThrownBy(()->acts.create(act(item,LocalDate.of(2094,1,1),new BigDecimal("3")))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("превышает остаток");
    }

    @Test void transferBalancesAreIndependentForAllocations(){ProjectItem item=projectItem("ИТ808.00.00.000","Болт",new BigDecimal("2"));ProjectItemAllocation second=new ProjectItemAllocation();second.setProjectItem(item);second.setQuantity(new BigDecimal("3"));item.getAllocations().add(second);projectItems.saveAndFlush(item);TransferAct act=baseAct(item,LocalDate.of(2099,1,1));TransferActItem line=new TransferActItem();line.setProjectItemAllocation(allocation(item));line.setQuantity(new BigDecimal("1"));act.getItems().add(line);TransferAct saved=acts.create(act);acts.finalizeAct(saved.getId());assertThat(acts.remaining(allocation(item))).isEqualByComparingTo("1");assertThat(acts.remaining(second)).isEqualByComparingTo("3");}
    @Test void newActDefaultsDateAndSnapshotsSubsection(){ProjectItem item=projectItem("ИТ809.00.00.000","Клапан",BigDecimal.ONE);ProjectSubsection subsection=allocation(item).getProjectSubsection();subsection.setDesignation("ИТ809.03.01.000");subsection.setAppliesFor("ИТ809.03.01.001_Труба");subsections.saveAndFlush(subsection);TransferAct prepared=acts.prepare(item.getProject().getId(),List.of(allocation(item).getId()));assertThat(prepared.getActDate()).isEqualTo(LocalDate.now());assertThat(prepared.getItems().getFirst().getDestinationDesignation()).isEqualTo("ИТ809.03.01.000");assertThat(prepared.getItems().getFirst().getAppliesFor()).isEqualTo("ИТ809.03.01.001_Труба");prepared.setDeliveredBy("Сотрудник 1");prepared.setReceivedBy("Сотрудник 2");TransferAct saved=acts.create(prepared);subsection.setDesignation("ИЗМЕНЕНО");subsections.saveAndFlush(subsection);assertThat(acts.findById(saved.getId()).getItems().getFirst().getDestinationDesignation()).isEqualTo("ИТ809.03.01.000");}

    @Test void allocationWithoutSubsectionCannotBePreparedForTransfer(){ProjectItem item=projectItem("ИТ811.00.00.000","Труба",BigDecimal.ONE);allocation(item).setProjectSubsection(null);projectItems.saveAndFlush(item);assertThatThrownBy(()->acts.prepare(item.getProject().getId(),List.of(allocation(item).getId()))).hasMessageContaining("подраздел");}

    private Organization organization(String name,Set<OrganizationRole> roles){Organization value=new Organization();value.setName(name);value.setRoles(roles);return value;}
    private ProjectItem projectItem(String designation,String itemName,BigDecimal quantity){Project project=new Project();project.setDesignation(designation);project.setName("Проект");project=projects.save(project);CatalogItem catalog=new CatalogItem();catalog.setName(itemName);catalog.setMeasurementUnit(units.findByName("шт.").orElseThrow());catalog=catalogItems.save(catalog);ProjectAssembly assembly=new ProjectAssembly();assembly.setProject(project);assembly.setName("Раздел");assembly=assemblies.save(assembly);ProjectSubsection subsection=new ProjectSubsection();subsection.setProjectAssembly(assembly);subsection.setDesignation(designation+".01");subsection=subsections.save(subsection);ProjectItem item=new ProjectItem();item.setProject(project);item.setCatalogItem(catalog);ProjectItemAllocation allocation=new ProjectItemAllocation();allocation.setProjectItem(item);allocation.setProjectAssembly(assembly);allocation.setProjectSubsection(subsection);allocation.setQuantity(quantity);item.getAllocations().add(allocation);return projectItems.save(item);}
    private TransferAct act(ProjectItem item,LocalDate date,BigDecimal quantity){TransferAct act=baseAct(item,date);act.getItems().add(line(item,quantity.toPlainString()));return act;}
    private TransferAct baseAct(ProjectItem item,LocalDate date){TransferAct act=new TransferAct();act.setProject(item.getProject());act.setActDate(date);act.setDeliveredBy("Сотрудник 1");act.setReceivedBy("Сотрудник 2");return act;}
    private TransferActItem line(ProjectItem item,String quantity){TransferActItem line=new TransferActItem();line.setProjectItemAllocation(allocation(item));line.setQuantity(new BigDecimal(quantity));return line;}
    private ProjectItemAllocation allocation(ProjectItem item){return item.getAllocations().getFirst();}
}
