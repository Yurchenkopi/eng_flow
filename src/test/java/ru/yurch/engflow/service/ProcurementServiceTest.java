package ru.yurch.engflow.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.model.CatalogItem;
import ru.yurch.engflow.model.ItemSupplier;
import ru.yurch.engflow.model.Organization;
import ru.yurch.engflow.model.OrganizationRole;
import ru.yurch.engflow.model.Procurement;
import ru.yurch.engflow.model.ProcurementLine;
import ru.yurch.engflow.model.ProcurementStatus;
import ru.yurch.engflow.model.Project;
import ru.yurch.engflow.model.ProjectItem;
import ru.yurch.engflow.model.ProjectItemAllocation;
import ru.yurch.engflow.model.SupplierInvoice;
import ru.yurch.engflow.model.SupplierInvoiceLine;
import ru.yurch.engflow.repository.CatalogItemRepository;
import ru.yurch.engflow.repository.ItemSupplierRepository;
import ru.yurch.engflow.repository.MeasurementUnitRepository;
import ru.yurch.engflow.repository.OrganizationRepository;
import ru.yurch.engflow.repository.ProjectItemRepository;
import ru.yurch.engflow.repository.ProjectRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@TestPropertySource(
        properties = {
                "engflow.invoice-storage=target/test-invoices",
                "spring.datasource.url=jdbc:h2:mem:eng_flow_procurement;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE"})
class ProcurementServiceTest {

    @Autowired
    ProcurementService procurements;
    @Autowired
    SupplierInvoiceService invoices;
    @Autowired
    ProjectItemService projectItemService;
    @Autowired
    ProjectRepository projects;
    @Autowired
    OrganizationRepository organizations;
    @Autowired
    CatalogItemRepository catalogItems;
    @Autowired
    MeasurementUnitRepository units;
    @Autowired
    ProjectItemRepository projectItems;
    @Autowired
    ItemSupplierRepository itemSuppliers;

    @Test
    void projectSupportsMultipleProcurementsAndItemCanUseTwoSuppliers() {
        Project project = project("ИТ901");
        ProjectItem item = item(project, "Клапан", "12");
        Procurement first = procurement(project, supplier("A"));
        Procurement second = procurement(project, supplier("B"));
        addLine(first, item, "2");
        addLine(second, item, "10");
        assertThat(procurements.findByProject(project.getId())).hasSize(2);
        assertThat(procurements.lines(first.getId())).hasSize(1);
        assertThat(procurements.progress(item).requestedQuantity()).isZero();
    }

    @Test
    void rfqAndPaymentFactsDetermineStatusAndAggregateAcrossSuppliers() {
        Project project = project("ИТ902");
        ProjectItem item = item(project, "Датчик", "12");
        assertThat(procurements.progress(item).status()).isEqualTo(ProcurementStatus.NOT_PLANNED);
        Procurement first = procurement(project, supplier("A"));
        Procurement second = procurement(project, supplier("B"));
        ProcurementLine firstLine = addLine(first, item, "2");
        ProcurementLine secondLine = addLine(second, item, "10");
        assertThat(procurements.progress(item).status()).isEqualTo(ProcurementStatus.PLANNED);
        procurements.markRfqSent(project.getId(), first.getId());
        assertThat(procurements.progress(item).status()).isEqualTo(ProcurementStatus.PARTIALLY_REQUESTED);
        SupplierInvoice paid = invoice(project, first, "100");
        addInvoiceLine(project, first, paid, firstLine, "2");
        SupplierInvoice pending = invoice(project, second, "101");
        addInvoiceLine(project, second, pending, secondLine, "10");
        assertThat(procurements.progress(item).status()).isEqualTo(ProcurementStatus.PARTIALLY_REQUESTED);
        invoices.submitForPayment(project.getId(), first.getId(), paid.getId());
        ProcurementProgress progress = procurements.progress(item);
        assertThat(progress.status()).isEqualTo(ProcurementStatus.PARTIALLY_ORDERED);
        assertThat(progress.orderedQuantity()).isEqualByComparingTo("2");
        assertThat(progress.getDisplay()).isEqualTo("Заказано: 2 / 12");
        invoices.submitForPayment(project.getId(), second.getId(), pending.getId());
        assertThat(procurements.progress(item).orderedQuantity()).isEqualByComparingTo("12");
    }

    @Test
    void procurementLineCanSplitAcrossInvoicesButCannotExceedRequested() {
        Project project = project("ИТ903");
        ProjectItem item = item(project, "Труба", "10");
        Procurement procurement = procurement(project, supplier("A"));
        ProcurementLine line = addLine(procurement, item, "10");
        SupplierInvoice first = invoice(project, procurement, "100");
        SupplierInvoice second = invoice(project, procurement, "101");
        addInvoiceLine(project, procurement, first, line, "4");
        addInvoiceLine(project, procurement, second, line, "6");
        SupplierInvoice third = invoice(project, procurement, "102");
        assertThatThrownBy(() -> addInvoiceLine(project, procurement, third, line, "1")).hasMessageContaining("превышает");
    }

    @Test
    void invoiceCanContainMultipleProcurementLines() {
        Project project = project("ИТ904");
        ProjectItem first = item(project, "Клапан", "3");
        ProjectItem second = item(project, "Штуцер", "4");
        Procurement procurement = procurement(project, supplier("A"));
        ProcurementLine firstLine = addLine(procurement, first, "3");
        ProcurementLine secondLine = addLine(procurement, second, "4");
        SupplierInvoice invoice = invoice(project, procurement, "200");
        addInvoiceLine(project, procurement, invoice, firstLine, "3");
        addInvoiceLine(project, procurement, invoice, secondLine, "4");
        assertThat(invoices.lines(invoice.getId())).hasSize(2);
    }

    @Test
    void rejectsWrongSupplierProjectAndProcurementReferences() {
        Project firstProject = project("ИТ905");
        Project secondProject = project("ИТ906");
        ProjectItem foreign = item(secondProject, "Чужая позиция", "1");
        Procurement invalid = new Procurement();
        Organization customer = supplier("Заказчик");
        customer.getRoles().clear();
        customer.getRoles().add(OrganizationRole.CUSTOMER);
        organizations.save(customer);
        invalid.setSupplier(customer);
        assertThatThrownBy(() -> procurements.create(firstProject.getId(), invalid)).hasMessageContaining("роли поставщика");
        Procurement first = procurement(firstProject, supplier("A"));
        ProcurementLine foreignLine = new ProcurementLine();
        foreignLine.setProjectItem(foreign);
        foreignLine.setRequestedQuantity(BigDecimal.ONE);
        assertThatThrownBy(() -> procurements.addLine(firstProject.getId(), first.getId(), foreignLine)).hasMessageContaining("не найдена");
        ProjectItem local = item(firstProject, "Своя позиция", "2");
        ProcurementLine localLine = addLine(first, local, "2");
        Procurement other = procurement(firstProject, supplier("B"));
        SupplierInvoice otherInvoice = invoice(firstProject, other, "300");
        SupplierInvoiceLine wrong = new SupplierInvoiceLine();
        wrong.setProcurementLine(localLine);
        wrong.setQuantity(BigDecimal.ONE);
        assertThatThrownBy(() -> invoices.addLine(firstProject.getId(), other.getId(), otherInvoice.getId(), wrong))
                .hasMessageContaining("не принадлежит");
    }

    @Test
    void invoiceFileIsStoredReadableReplaceableAndRemovable() {
        Project project = project("ИТ907");
        Procurement procurement = procurement(project, supplier("A"));
        SupplierInvoice value = new SupplierInvoice();
        value.setInvoiceNumber("400");
        value.setInvoiceDate(LocalDate.now());
        MockMultipartFile file = new MockMultipartFile("file", "invoice.pdf", "application/pdf", "pdf-data".getBytes());
        SupplierInvoice saved = invoices.create(project.getId(), procurement.getId(), value, file);
        assertThat(saved.getFileOriginalName()).isEqualTo("invoice.pdf");
        assertThat(invoices.file(project.getId(), procurement.getId(), saved.getId()).exists()).isTrue();
        SupplierInvoice update = new SupplierInvoice();
        update.setInvoiceNumber(saved.getInvoiceNumber());
        update.setInvoiceDate(saved.getInvoiceDate());
        MockMultipartFile replacement = new MockMultipartFile("file", "replacement.png", "image/png", "image-data".getBytes());
        invoices.update(project.getId(), procurement.getId(), saved.getId(), update, replacement);
        assertThat(invoices.find(project.getId(), procurement.getId(), saved.getId()).getFileOriginalName()).isEqualTo("replacement.png");
        invoices.deleteFile(project.getId(), procurement.getId(), saved.getId());
        assertThat(invoices.find(project.getId(), procurement.getId(), saved.getId()).hasFile()).isFalse();
    }

    @Test
    void createsNumberedProcurementFromSelectedConfigurationRows() {
        Project project = project("ИТ908");
        ProjectItem first = item(project, "\u041a\u043b\u0430\u043f\u0430\u043d", "4");
        ProjectItem second = item(project, "\u0428\u0442\u0443\u0446\u0435\u0440", "6");
        Organization supplier = supplier("Пневматика");
        relate(first, supplier);
        relate(second, supplier);
        Procurement saved = procurements.createFromConfiguration(project.getId(), supplier.getId(), Set.of(first.getId(), second.getId()),
                false);
        assertThat(saved.getSupplier().getId()).isEqualTo(supplier.getId());
        assertThat(saved.getSequenceNumber()).isEqualTo(1);
        assertThat(saved.getDisplayNumber()).startsWith("Пневматика_001_");
        assertThat(saved.getRfqSentAt()).isNull();
        assertThat(procurements.lines(saved.getId())).hasSize(2);
    }

    @Test
    void configurationCreationRequiresRowsAndSupplier() {
        Project project = project("ИТ908-1");
        ProjectItem item = item(project, "Клапан", "4");
        assertThatThrownBy(() -> procurements.createFromConfiguration(project.getId(), null, Set.of(item.getId()), true))
                .hasMessageContaining("поставщика");
        assertThatThrownBy(() -> procurements.createFromConfiguration(project.getId(), supplier("A").getId(), Set.of(), true))
                .hasMessageContaining("позицию");
    }

    @Test
    void pieceQuantityUsesWholeNumberStep() {
        Project project = project("ИТ908-STEP");
        ProjectItem item = item(project, "Патрубок", "3");
        Procurement procurement = procurement(project, supplier("A-step"));
        assertThatThrownBy(() -> addLine(procurement, item, "2.9999")).hasMessageContaining("шагом 1");
        assertThat(addLine(procurement, item, "2").getRequestedQuantity()).isEqualByComparingTo("2");
    }

    @Test
    void deletingPartiallyPlannedItemRemovesOnlyUnplannedRemainder() {
        Project project = project("ИТ908-DELETE");
        ProjectItem item = item(project, "Патрубок удаления", "5");
        Procurement procurement = procurement(project, supplier("Delete supplier"));
        addLine(procurement, item, "2");
        assertThat(projectItemService.delete(project.getId(), item.getId())).isEqualByComparingTo("3");
        assertThat(projectItemService.findByProjectAndId(project.getId(), item.getId()).getRequiredQuantity()).isEqualByComparingTo("2");
        assertThatThrownBy(() -> projectItemService.delete(project.getId(), item.getId())).hasMessageContaining("все количество");
    }

    @Test
    void unlistedSupplierNeedsExplicitContinue() {
        Project project = project("ИТ909");
        ProjectItem item = item(project, "Клапан", "4");
        Organization supplier = supplier("Другой");
        assertThatThrownBy(() -> procurements.createFromConfiguration(project.getId(), supplier.getId(), Set.of(item.getId()), false))
                .hasMessageContaining("Подтвердите");
        assertThat(
                procurements.createFromConfiguration(project.getId(), supplier.getId(), Set.of(item.getId()), true).getSupplier().getId())
                .isEqualTo(supplier.getId());
    }

    @Test
    void sequenceIsScopedByProjectAndSupplierAndDeletedNumberIsNotReused() {
        Project firstProject = project("\u0418\u0422910");
        Project secondProject = project("\u0418\u0422911");
        Organization a = supplier("A");
        Organization b = supplier("B");
        Procurement a1 = procurement(firstProject, a);
        Procurement a2 = procurement(firstProject, a);
        Procurement b1 = procurement(firstProject, b);
        Procurement otherProjectA1 = procurement(secondProject, a);
        assertThat(a1.getSequenceNumber()).isEqualTo(1);
        assertThat(a2.getSequenceNumber()).isEqualTo(2);
        assertThat(b1.getSequenceNumber()).isEqualTo(1);
        assertThat(otherProjectA1.getSequenceNumber()).isEqualTo(1);
        procurements.delete(firstProject.getId(), a2.getId());
        assertThat(procurement(firstProject, a).getSequenceNumber()).isEqualTo(3);
    }

    @Test
    void deletingProcurementRemovesOnlyLinesAndRecalculatesPlanned() {
        Project project = project("ИТ912");
        Organization supplier = supplier("A");
        ProjectItem item = item(project, "Клапан", "10");
        Procurement procurement = procurement(project, supplier);
        addLine(procurement, item, "4");
        assertThat(procurements.progress(item).status()).isEqualTo(ProcurementStatus.PLANNED);
        procurements.delete(project.getId(), procurement.getId());
        assertThat(procurements.progress(item).status()).isEqualTo(ProcurementStatus.NOT_PLANNED);
        assertThat(projectItems.existsById(item.getId())).isTrue();
        assertThat(organizations.existsById(supplier.getId())).isTrue();
        assertThat(catalogItems.existsById(item.getCatalogItem().getId())).isTrue();
    }

    @Test
    void procurementWithInvoiceCannotBeDeleted() {
        Project project = project("ИТ913");
        Procurement procurement = procurement(project, supplier("A"));
        invoice(project, procurement, "500");
        assertThatThrownBy(() -> procurements.delete(project.getId(), procurement.getId())).hasMessageContaining("счет поставщика");
    }

    @Test
    void configurationDefaultsToRemainingAndEditingCannotExceedOtherPlans() {
        Project project = project("ИТ914");
        ProjectItem item = item(project, "Клапан", "10");
        Organization firstSupplier = supplier("A");
        Organization secondSupplier = supplier("B");
        relate(item, firstSupplier);
        relate(item, secondSupplier);
        Procurement first = procurements.createFromConfiguration(project.getId(), firstSupplier.getId(), Set.of(item.getId()), false);
        ProcurementLine firstLine = procurements.lines(first.getId()).getFirst();
        assertThat(firstLine.getRequestedQuantity()).isEqualByComparingTo("10");
        procurements.updateLineQuantity(project.getId(), first.getId(), firstLine.getId(), new BigDecimal("4"));
        Procurement second = procurements.createFromConfiguration(project.getId(), secondSupplier.getId(), Set.of(item.getId()), false);
        ProcurementLine secondLine = procurements.lines(second.getId()).getFirst();
        assertThat(secondLine.getRequestedQuantity()).isEqualByComparingTo("6");
        assertThat(procurements.maxAllowed(secondLine)).isEqualByComparingTo("6");
        assertThatThrownBy(() -> procurements.updateLineQuantity(project.getId(), second.getId(), secondLine.getId(), new BigDecimal("7")))
                .hasMessageContaining("допустимый остаток");
        assertThat(procurements.maxAllowed(firstLine)).isEqualByComparingTo("4");
        assertThatThrownBy(() -> procurements.updateLineQuantity(project.getId(), first.getId(), firstLine.getId(), new BigDecimal("5")))
                .hasMessageContaining("допустимый остаток");
    }

    @Test
    void lineQuantityIsLockedAfterRfqOrInvoiceLine() {
        Project project = project("ИТ915");
        ProjectItem item = item(project, "Датчик", "5");
        Procurement rfq = procurement(project, supplier("A"));
        ProcurementLine rfqLine = addLine(rfq, item, "2");
        procurements.markRfqSent(project.getId(), rfq.getId());
        assertThatThrownBy(() -> procurements.updateLineQuantity(project.getId(), rfq.getId(), rfqLine.getId(), BigDecimal.ONE))
                .hasMessageContaining("нельзя изменить");
        Procurement invoiced = procurement(project, supplier("B"));
        ProcurementLine invoicedLine = addLine(invoiced, item, "3");
        SupplierInvoice invoice = invoice(project, invoiced, "600");
        addInvoiceLine(project, invoiced, invoice, invoicedLine, "1");
        assertThatThrownBy(
                () -> procurements.updateLineQuantity(project.getId(), invoiced.getId(), invoicedLine.getId(), new BigDecimal("2")))
                .hasMessageContaining("нельзя изменить");
    }

    @Test
    @org.springframework.transaction.annotation.Transactional(
            propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void lineViewCalculationsWorkAfterProcurementLinesLeavePersistenceContext() {
        Project project = project("ИТ915-DETACHED");
        ProjectItem item = item(project, "Клапан отсоединенный", "5");
        Procurement procurement = procurement(project, supplier("Detached supplier"));
        addLine(procurement, item, "5");
        ProcurementLine detachedLine = procurements.lines(procurement.getId()).getFirst();
        assertThat(procurements.maxAllowed(detachedLine)).isEqualByComparingTo("5");
        assertThat(procurements.isLineQuantityEditable(detachedLine)).isTrue();
        procurements.markRfqSent(project.getId(), procurement.getId());
        assertThat(procurements.isLineQuantityEditable(detachedLine)).isFalse();
    }

    @Test
    void statusUsesMostAdvancedFactAndKeepsPartialRequestedQuantity() {
        Project project = project("ИТ916");
        ProjectItem item = item(project, "Манометр", "10");
        Procurement sent = procurement(project, supplier("A"));
        ProcurementLine sentLine = addLine(sent, item, "4");
        Procurement planned = procurement(project, supplier("B"));
        ProcurementLine plannedLine = addLine(planned, item, "6");
        assertThat(procurements.progress(item).getDisplay()).isEqualTo("В закупке: 10 / 10");
        procurements.markRfqSent(project.getId(), sent.getId());
        ProcurementProgress requested = procurements.progress(item);
        assertThat(requested.plannedQuantity()).isEqualByComparingTo("10");
        assertThat(requested.requestedQuantity()).isEqualByComparingTo("4");
        assertThat(requested.getDisplay()).isEqualTo("Запрошено: 4 / 10");
        SupplierInvoice pending = invoice(project, sent, "700");
        addInvoiceLine(project, sent, pending, sentLine, "4");
        assertThat(procurements.progress(item).getDisplay()).isEqualTo("Запрошено: 4 / 10");
        invoices.submitForPayment(project.getId(), sent.getId(), pending.getId());
        assertThat(procurements.progress(item).getDisplay()).isEqualTo("Заказано: 4 / 10");
        procurements.markRfqSent(project.getId(), planned.getId());
        assertThat(procurements.progress(item).getDisplay()).isEqualTo("Заказано: 4 / 10");
        SupplierInvoice fullyPaid = invoice(project, planned, "701");
        addInvoiceLine(project, planned, fullyPaid, plannedLine, "6");
        invoices.submitForPayment(project.getId(), planned.getId(), fullyPaid.getId());
        assertThat(procurements.progress(item).getDisplay()).isEqualTo("Заказано: 10 / 10");
    }

    private Project project(String designation) {
        Project value = new Project();
        value.setDesignation(designation);
        value.setName("Проект");
        return projects.save(value);
    }

    private Organization supplier(String name) {
        Organization value = new Organization();
        value.setName(name);
        value.getRoles().add(OrganizationRole.SUPPLIER);
        return organizations.save(value);
    }

    private ProjectItem item(Project project, String name, String quantity) {
        CatalogItem catalog = new CatalogItem();
        catalog.setName(name);
        catalog.setMeasurementUnit(units.findByName("шт.").orElseThrow());
        catalog = catalogItems.save(catalog);
        ProjectItem item = new ProjectItem();
        item.setProject(project);
        item.setCatalogItem(catalog);
        ProjectItemAllocation allocation = new ProjectItemAllocation();
        allocation.setProjectItem(item);
        allocation.setQuantity(new BigDecimal(quantity));
        item.getAllocations().add(allocation);
        return projectItems.save(item);
    }

    private void relate(ProjectItem item, Organization supplier) {
        ItemSupplier relation = new ItemSupplier();
        relation.setCatalogItem(item.getCatalogItem());
        relation.setSupplier(supplier);
        itemSuppliers.save(relation);
        item.getCatalogItem().getItemSuppliers().add(relation);
    }

    private Procurement procurement(Project project, Organization supplier) {
        Procurement value = new Procurement();
        value.setSupplier(supplier);
        return procurements.create(project.getId(), value);
    }

    private ProcurementLine addLine(Procurement procurement, ProjectItem item, String quantity) {
        ProcurementLine line = new ProcurementLine();
        line.setProjectItem(item);
        line.setRequestedQuantity(new BigDecimal(quantity));
        return procurements.addLine(procurement.getProject().getId(), procurement.getId(), line);
    }

    private SupplierInvoice invoice(Project project, Procurement procurement, String number) {
        SupplierInvoice value = new SupplierInvoice();
        value.setInvoiceNumber(number);
        value.setInvoiceDate(LocalDate.now());
        return invoices.create(project.getId(), procurement.getId(), value, null);
    }

    private SupplierInvoiceLine addInvoiceLine(
            Project project,
            Procurement procurement,
            SupplierInvoice invoice,
            ProcurementLine procurementLine,
            String quantity) {
        SupplierInvoiceLine line = new SupplierInvoiceLine();
        line.setProcurementLine(procurementLine);
        line.setQuantity(new BigDecimal(quantity));
        return invoices.addLine(project.getId(), procurement.getId(), invoice.getId(), line);
    }
}
