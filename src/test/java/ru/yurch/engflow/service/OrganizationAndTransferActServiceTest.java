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
    @Autowired OrganizationService organizations;@Autowired ContactService contacts;@Autowired ProjectRepository projects;@Autowired CatalogItemRepository catalogItems;@Autowired ProjectItemRepository projectItems;@Autowired TransferActService acts;

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
        assertThat(saved.isTransferred()).isFalse();assertThat(acts.transferred(item.getId())).isEqualByComparingTo("0");assertThat(acts.remaining(item)).isEqualByComparingTo("10");
        assertThat(loaded.getItems()).hasSize(2).allSatisfy(line->assertThat(line.getTotalSameCatalogItem()).isEqualByComparingTo("5"));
        acts.finalizeAct(saved.getId());assertThat(acts.transferred(item.getId())).isEqualByComparingTo("5");assertThat(acts.remaining(item)).isEqualByComparingTo("5");
    }

    @Test void partiallyAndFullyTransferredItemsHaveCorrectRemaining(){ProjectItem item=projectItem("ИТ804.00.00.000","Муфта",new BigDecimal("5"));TransferAct first=acts.create(act(item,LocalDate.of(2095,1,1),new BigDecimal("3")));acts.finalizeAct(first.getId());assertThat(acts.remaining(item)).isEqualByComparingTo("2");TransferAct second=acts.create(act(item,LocalDate.of(2095,1,2),new BigDecimal("2")));acts.finalizeAct(second.getId());assertThat(acts.remaining(item)).isZero();assertThatThrownBy(()->acts.prepare(item.getProject().getId(),List.of(item.getId()))).hasMessageContaining("уже передано");}

    @Test void emptyActCannotFinalizeAndDraftItemCanBeRemoved(){ProjectItem item=projectItem("ИТ805.00.00.000","Штуцер",new BigDecimal("2"));TransferAct saved=acts.create(act(item,LocalDate.of(2096,1,1),new BigDecimal("1")));Long lineId=acts.findById(saved.getId()).getItems().getFirst().getId();acts.removeItem(saved.getId(),lineId);assertThat(acts.findById(saved.getId()).getItems()).isEmpty();assertThatThrownBy(()->acts.finalizeAct(saved.getId())).hasMessageContaining("Пустой акт");}

    @Test void transferredActIsImmutable(){ProjectItem item=projectItem("ИТ806.00.00.000","Фланец",new BigDecimal("2"));TransferAct saved=acts.create(act(item,LocalDate.of(2097,1,1),new BigDecimal("1")));Long lineId=acts.findById(saved.getId()).getItems().getFirst().getId();acts.finalizeAct(saved.getId());assertThatThrownBy(()->acts.update(saved.getId(),saved)).isInstanceOf(IllegalStateException.class);assertThatThrownBy(()->acts.delete(saved.getId())).isInstanceOf(IllegalStateException.class);assertThatThrownBy(()->acts.removeItem(saved.getId(),lineId)).isInstanceOf(IllegalStateException.class);}

    @Test void draftDoesNotReduceAvailableQuantity(){ProjectItem item=projectItem("ИТ807.00.00.000","Кольцо",new BigDecimal("4"));acts.create(act(item,LocalDate.of(2098,1,1),new BigDecimal("3")));assertThat(acts.remaining(item)).isEqualByComparingTo("4");assertThat(acts.prepare(item.getProject().getId(),List.of(item.getId())).getItems().getFirst().getQuantity()).isEqualByComparingTo("4");}

    @Test void rejectsTransferAboveRequiredQuantity(){
        ProjectItem item=projectItem("ИТ803.00.00.000","Датчик",new BigDecimal("2"));
        assertThatThrownBy(()->acts.create(act(item,LocalDate.of(2094,1,1),new BigDecimal("2.1")))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("превышает остаток");
    }

    private Organization organization(String name,Set<OrganizationRole> roles){Organization value=new Organization();value.setName(name);value.setRoles(roles);return value;}
    private ProjectItem projectItem(String designation,String itemName,BigDecimal quantity){Project project=new Project();project.setDesignation(designation);project.setName("Проект");project=projects.save(project);CatalogItem catalog=new CatalogItem();catalog.setName(itemName);catalog.setUnit("шт.");catalog=catalogItems.save(catalog);ProjectItem item=new ProjectItem();item.setProject(project);item.setCatalogItem(catalog);item.setRequiredQuantity(quantity);return projectItems.save(item);}
    private TransferAct act(ProjectItem item,LocalDate date,BigDecimal quantity){TransferAct act=baseAct(item,date);act.getItems().add(line(item,quantity.toPlainString()));return act;}
    private TransferAct baseAct(ProjectItem item,LocalDate date){TransferAct act=new TransferAct();act.setProject(item.getProject());act.setActDate(date);act.setDeliveredBy("Сотрудник 1");act.setReceivedBy("Сотрудник 2");return act;}
    private TransferActItem line(ProjectItem item,String quantity){TransferActItem line=new TransferActItem();line.setProjectItem(item);line.setQuantity(new BigDecimal(quantity));return line;}
}
