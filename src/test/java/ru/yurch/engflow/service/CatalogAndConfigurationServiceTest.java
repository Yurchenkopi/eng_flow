package ru.yurch.engflow.service;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.controller.ProjectItemController;
import ru.yurch.engflow.model.CatalogItem;
import ru.yurch.engflow.model.ItemSupplier;
import ru.yurch.engflow.model.MeasurementUnit;
import ru.yurch.engflow.model.Organization;
import ru.yurch.engflow.model.OrganizationRole;
import ru.yurch.engflow.model.Project;
import ru.yurch.engflow.model.ProjectAssembly;
import ru.yurch.engflow.model.ProjectItem;
import ru.yurch.engflow.model.ProjectItemAllocation;
import ru.yurch.engflow.model.ProjectSubsection;
import ru.yurch.engflow.repository.ItemSupplierRepository;
import ru.yurch.engflow.repository.MeasurementUnitRepository;
import ru.yurch.engflow.repository.OrganizationRepository;
import ru.yurch.engflow.repository.ProjectAssemblyRepository;
import ru.yurch.engflow.repository.ProjectItemRepository;
import ru.yurch.engflow.repository.ProjectRepository;
import ru.yurch.engflow.repository.ProjectSubsectionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class CatalogAndConfigurationServiceTest {

    @Autowired
    CatalogItemService catalogService;
    @Autowired
    ProjectItemService projectItemService;
    @Autowired
    ProjectRepository projects;
    @Autowired
    ProjectItemRepository projectItems;
    @Autowired
    ProjectAssemblyRepository assemblies;
    @Autowired
    ProjectSubsectionRepository subsections;
    @Autowired
    ProjectItemController projectItemController;
    @Autowired
    MeasurementUnitRepository units;
    @Autowired
    OrganizationRepository organizations;
    @Autowired
    ItemSupplierRepository itemSuppliers;
    private CatalogItem valve;
    private CatalogItem tube;

    @BeforeEach
    void setUp() {
        valve = item("VAT-10", "Клапан", "VAT");
        tube = item("DIN-20", "Труба", "Example");
        valve = catalogService.create(valve);
        tube = catalogService.create(tube);
    }

    @Test
    void searchesByDesignationNameAndIgnoresCase() {
        assertThat(catalogService.autocomplete("vat-10")).extracting(CatalogItem::getId).contains(valve.getId());
        assertThat(catalogService.autocomplete("КЛАПАН")).extracting(CatalogItem::getId).contains(valve.getId());
        assertThat(catalogService.autocomplete("труба")).extracting(CatalogItem::getId).contains(tube.getId());
    }

    @Test
    void baseMeasurementUnitsExistAndCatalogUsesReference() {
        assertThat(units.findAllByOrderByIdAsc()).extracting(MeasurementUnit::getName).contains("шт.", "мм", "м", "кг", "г", "л", "компл.");
        assertThat(valve.getMeasurementUnit().getName()).isEqualTo("шт.");
    }

    @Test
    void allocationMayOmitSubsectionAndRejectsSubsectionFromAnotherAssembly() {
        Project project = project("ИТ712.00.00.000");
        ProjectAssembly first = assembly(project, "Первый");
        ProjectAssembly second = assembly(project, "Второй");
        ProjectSubsection foreign = new ProjectSubsection();
        foreign.setProjectAssembly(second);
        foreign.setDesignation("ИТ712.02.01.000");
        foreign = subsections.save(foreign);
        ProjectItem plain = projectItem(project, valve);
        plain.getAllocations().getFirst().setProjectAssembly(first);
        assertThat(projectItemService.create(project.getId(), plain).getAllocations().getFirst().getProjectSubsection()).isNull();
        ProjectItem detailed = projectItem(project, tube);
        ProjectItemAllocation allocation = detailed.getAllocations().getFirst();
        allocation.setProjectAssembly(first);
        allocation.setProjectSubsection(foreign);
        allocation.setDetailForTransfer(true);
        assertThatThrownBy(() -> projectItemService.create(project.getId(), detailed))
                .hasMessageContaining("принадлежать выбранному разделу");
    }

    @Test
    void putsExactDesignationFirst() {
        catalogService.create(item("X-VAT-10-X", "Другой клапан", "VAT"));
        assertThat(catalogService.autocomplete("vat-10").getFirst().getId()).isEqualTo(valve.getId());
    }

    @Test
    void copyCreatesNewItemWithoutChangingSource() {
        CatalogItem copy = catalogService.prepareCopy(valve.getId());
        copy.setName("Клапан, вариант");
        CatalogItem saved = catalogService.create(copy);
        assertThat(saved.getId()).isNotEqualTo(valve.getId());
        assertThat(catalogService.findById(valve.getId()).getName()).isEqualTo("Клапан");
    }

    @Test
    void configurationFilterDoesNotMixProjects() {
        Project first = project("\u0418\u0422701.00.00.000");
        Project second = project("\u0418\u0422702.00.00.000");
        projectItems.save(projectItem(first, valve));
        projectItems.save(projectItem(second, tube));
        assertThat(projectItemService.search(first.getId(), "", null, null, "designation", "asc"))
                .allMatch(item -> item.getProject().getId().equals(first.getId())).extracting(item -> item.getCatalogItem().getId())
                .containsExactly(valve.getId());
    }

    @Test
    void supplierFilterWithMultipleSuppliersReturnsOneItemAndCombinesWithTextSearch() {
        Project project = project("ИТ718.00.00.000");
        ProjectItem saved = projectItems.save(projectItem(project, valve));
        Organization first = supplier("\u041f\u043d\u0435\u0432\u043c\u0430\u0442\u0438\u043a\u0430");
        Organization second = supplier("\u0412\u0430\u043a\u0443\u0443\u043c");
        relation(valve, first);
        relation(valve, second);
        assertThat(projectItemService.search(project.getId(), "клапан", null, first.getId(), "name", "asc")).extracting(ProjectItem::getId)
                .containsExactly(saved.getId());
        assertThat(projectItemService.search(project.getId(), "нет", null, first.getId(), "name", "asc")).isEmpty();
        assertThat(projectItemService.search(project.getId(), "", null, second.getId(), "name", "asc")).hasSize(1).first()
                .satisfies(item -> assertThat(item.getCatalogItem().getItemSuppliers()).hasSize(2));
    }

    @Test
    void projectItemIsUniquePerProjectAndCatalogAndQuantityIsAllocationSum() {
        Project project = project("ИТ703.00.00.000");
        ProjectItem first = projectItem(project, valve);
        ProjectItemAllocation second = new ProjectItemAllocation();
        second.setQuantity(BigDecimal.ONE);
        second.setProjectAssembly(assembly(project, "Камера"));
        first.getAllocations().add(second);
        ProjectItem saved = projectItemService.create(project.getId(), first);
        assertThat(saved.getRequiredQuantity()).isEqualByComparingTo("2.0");
        assertThatThrownBy(() -> projectItemService.create(project.getId(), projectItem(project, valve)))
                .isInstanceOf(ProjectItemService.DuplicateProjectItemException.class);
    }

    @Test
    void configurationSearchReturnsUniqueItemsAndLoadsEveryAllocation() {
        Project project = project("ИТ704.00.00.000");
        ProjectAssembly chamber = assembly(project, "Вакуумная камера");
        ProjectAssembly cooling = assembly(project, "Охлаждение");
        ProjectItem item = projectItem(project, valve);
        item.getAllocations().getFirst().setProjectAssembly(chamber);
        ProjectItemAllocation second = new ProjectItemAllocation();
        second.setProjectItem(item);
        second.setProjectAssembly(cooling);
        second.setQuantity(new BigDecimal("2.5"));
        item.getAllocations().add(second);
        projectItems.saveAndFlush(item);
        assertSearchHasOneItemWithBothAllocations(project, "", null);
        assertSearchHasOneItemWithBothAllocations(project, "", chamber.getId());
        assertSearchHasOneItemWithBothAllocations(project, "vat", null);
        assertSearchHasOneItemWithBothAllocations(project, "вакуумная", null);
    }

    @Test
    void rejectsRepeatedAssemblyIncludingWithoutAssembly() {
        Project project = project("ИТ705.00.00.000");
        ProjectItem noAssembly = projectItem(project, valve);
        ProjectItemAllocation duplicate = new ProjectItemAllocation();
        duplicate.setQuantity(BigDecimal.ONE);
        noAssembly.getAllocations().add(duplicate);
        assertThatThrownBy(() -> projectItemService.create(project.getId(), noAssembly)).hasMessageContaining("Обычная строка");
        ProjectAssembly chamber = assembly(project, "Камера");
        ProjectItem sameAssembly = projectItem(project, tube);
        sameAssembly.getAllocations().getFirst().setProjectAssembly(chamber);
        ProjectItemAllocation repeated = new ProjectItemAllocation();
        repeated.setProjectAssembly(chamber);
        repeated.setQuantity(BigDecimal.ONE);
        sameAssembly.getAllocations().add(repeated);
        assertThatThrownBy(() -> projectItemService.create(project.getId(), sameAssembly)).hasMessageContaining("один раз");
    }

    @Test
    void sameSectionSupportsDifferentSubsectionsAndSectionOnlyAllocation() {
        Project project = project("ИТ713.00.00.000");
        ProjectAssembly section = assembly(project, "Вакуумная система");
        ProjectItem item = projectItem(project, valve);
        item.getAllocations().getFirst().setProjectAssembly(section);
        ProjectItemAllocation first = new ProjectItemAllocation();
        first.setProjectAssembly(section);
        first.setQuantity(new BigDecimal("7"));
        first.setDetailForTransfer(true);
        first.setSubsectionDesignation("ИТ713.03.01.000");
        ProjectItemAllocation second = new ProjectItemAllocation();
        second.setProjectAssembly(section);
        second.setQuantity(BigDecimal.ONE);
        second.setDetailForTransfer(true);
        second.setSubsectionDesignation("ИТ713.03.02.000");
        item.getAllocations().add(first);
        item.getAllocations().add(second);
        ProjectItem saved = projectItemService.create(project.getId(), item);
        assertThat(saved.getAllocations()).hasSize(3);
        assertThat(saved.getSectionSummary()).isEqualTo("Вакуумная система");
    }

    @Test
    void duplicateSectionAndSubsectionIsRejected() {
        Project project = project("ИТ714.00.00.000");
        ProjectAssembly section = assembly(project, "Вакуумная система");
        ProjectItem item = projectItem(project, valve);
        item.getAllocations().clear();
        for (int index = 0; index < 2; index++) {
            ProjectItemAllocation allocation = new ProjectItemAllocation();
            allocation.setProjectAssembly(section);
            allocation.setQuantity(BigDecimal.ONE);
            allocation.setDetailForTransfer(true);
            allocation.setSubsectionDesignation("ИТ714.03.01.000");
            item.getAllocations().add(allocation);
        }
        assertThatThrownBy(() -> projectItemService.create(project.getId(), item)).hasMessageContaining("подраздел");
    }

    @Test
    void sameSubsectionSupportsDifferentFreeTextAppliesFor() {
        Project project = project("ИТ716.00.00.000");
        ProjectAssembly section = assembly(project, "Вакуумная система");
        ProjectItem item = projectItem(project, valve);
        item.getAllocations().clear();
        ProjectItemAllocation first = detailed(section, "ИТ716.03.01.000", "Прочие изделия", new BigDecimal("2"));
        ProjectItemAllocation second = detailed(section, "ИТ716.03.01.000", "ИТ716.04.01.001", new BigDecimal("3"));
        item.getAllocations().add(first);
        item.getAllocations().add(second);
        ProjectItem saved = projectItemService.create(project.getId(), item);
        assertThat(saved.getAllocations()).hasSize(2).extracting(ProjectItemAllocation::getAppliesFor)
                .containsExactlyInAnyOrder("Прочие изделия", "ИТ716.04.01.001");
        assertThat(saved.getAllocations()).extracting(a -> a.getProjectSubsection().getId()).doesNotContainNull()
                .containsOnly(saved.getAllocations().getFirst().getProjectSubsection().getId());
    }

    @Test
    void duplicateSubsectionAndAppliesForIsRejected() {
        Project project = project("ИТ717.00.00.000");
        ProjectAssembly section = assembly(project, "Вакуумная система");
        ProjectItem item = projectItem(project, valve);
        item.getAllocations().clear();
        item.getAllocations().add(detailed(section, "ИТ717.03.01.000", "Прочие изделия", BigDecimal.ONE));
        item.getAllocations().add(detailed(section, "ИТ717.03.01.000", "Прочие изделия", BigDecimal.ONE));
        assertThatThrownBy(() -> projectItemService.create(project.getId(), item)).hasMessageContaining("назначения");
    }

    @Test
    void projectItemWithoutActReferencesCanBeDeleted() {
        Project project = project("ИТ715.00.00.000");
        ProjectItem saved = projectItemService.create(project.getId(), projectItem(project, valve));
        projectItemService.delete(project.getId(), saved.getId());
        assertThat(projectItems.existsById(saved.getId())).isFalse();
    }

    @Test
    void quantityStepDependsOnCatalogUnit() {
        valve.setMeasurementUnit(unit("шт."));
        assertThat(valve.getQuantityStep()).isEqualByComparingTo("1.0");
        valve.setMeasurementUnit(unit("кг"));
        assertThat(valve.getQuantityStep()).isEqualByComparingTo("0.1");
        valve.setMeasurementUnit(unit("мм"));
        assertThat(valve.getQuantityStep()).isEqualByComparingTo("0.0001");
    }

    @Test
    void backendAppliesQuantityStepForPiecesKilogramsAndMeters() {
        Project pieces = project("ИТ708.00.00.000");
        ProjectItem invalid = projectItem(pieces, valve);
        invalid.getAllocations().getFirst().setQuantity(new BigDecimal("1.5"));
        assertThatThrownBy(() -> projectItemService.create(pieces.getId(), invalid)).hasMessageContaining("шагу");
        CatalogItem kilograms = item("KG-1", "Материал", "Example");
        kilograms.setMeasurementUnit(unit("кг"));
        kilograms = catalogService.create(kilograms);
        Project kgProject = project("ИТ709.00.00.000");
        ProjectItem kg = projectItem(kgProject, kilograms);
        kg.getAllocations().getFirst().setQuantity(new BigDecimal("1.2"));
        assertThat(projectItemService.create(kgProject.getId(), kg).getRequiredQuantity()).isEqualByComparingTo("1.2");
        CatalogItem meters = item("M-1", "Труба", "Example");
        meters.setMeasurementUnit(unit("м"));
        meters = catalogService.create(meters);
        Project mProject = project("ИТ710.00.00.000");
        ProjectItem m = projectItem(mProject, meters);
        m.getAllocations().getFirst().setQuantity(new BigDecimal("0.3"));
        assertThat(projectItemService.create(mProject.getId(), m).getRequiredQuantity()).isEqualByComparingTo("0.3");
    }

    @Test
    void sectionSummaryUsesNameAndRussianCount() {
        Project project = project("ИТ706.00.00.000");
        ProjectAssembly chamber = assembly(project, "Камера");
        ProjectItem value = projectItem(project, valve);
        value.getAllocations().getFirst().setProjectAssembly(chamber);
        assertThat(value.getSectionSummary()).isEqualTo("Камера");
        ProjectItemAllocation second = new ProjectItemAllocation();
        second.setQuantity(BigDecimal.ONE);
        value.getAllocations().add(second);
        assertThat(value.getSectionSummary()).isEqualTo("2 раздела");
    }

    @Test
    void existingCatalogSelectionReturnsImmediateEditUrl() {
        Project project = project("ИТ707.00.00.000");
        ProjectItem saved = projectItemService.create(project.getId(), projectItem(project, valve));
        assertThat(projectItemController.existing(project.getId(), valve.getId())).containsEntry("exists", true).containsEntry("url",
                "/projects/" + project.getId() + "/items/" + saved.getId() + "/edit?alreadyExists=1");
    }

    private CatalogItem item(String designation, String name, String manufacturer) {
        CatalogItem item = new CatalogItem();
        item.setDesignation(designation);
        item.setName(name);
        item.setManufacturer(manufacturer);
        item.setMeasurementUnit(unit("шт."));
        return item;
    }

    private MeasurementUnit unit(String name) {
        return units.findByName(name).orElseThrow();
    }

    private Project project(String designation) {
        Project project = new Project();
        project.setDesignation(designation);
        project.setName("Проект");
        return projects.save(project);
    }

    private ProjectAssembly assembly(Project project, String name) {
        ProjectAssembly value = new ProjectAssembly();
        value.setProject(project);
        value.setName(name);
        return assemblies.save(value);
    }

    private Organization supplier(String name) {
        Organization value = new Organization();
        value.setName(name);
        value.getRoles().add(OrganizationRole.SUPPLIER);
        return organizations.save(value);
    }

    private void relation(CatalogItem item, Organization supplier) {
        ItemSupplier value = new ItemSupplier();
        value.setCatalogItem(item);
        value.setSupplier(supplier);
        itemSuppliers.save(value);
        item.getItemSuppliers().add(value);
    }

    private ProjectItemAllocation detailed(ProjectAssembly section, String subsection, String appliesFor, BigDecimal quantity) {
        ProjectItemAllocation allocation = new ProjectItemAllocation();
        allocation.setProjectAssembly(section);
        allocation.setQuantity(quantity);
        allocation.setDetailForTransfer(true);
        allocation.setSubsectionDesignation(subsection);
        allocation.setSubsectionAppliesFor(appliesFor);
        return allocation;
    }

    private void assertSearchHasOneItemWithBothAllocations(Project project, String query, Long assemblyId) {
        var result = projectItemService.search(project.getId(), query, assemblyId, null, "name", "asc");
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getAllocations()).hasSize(2);
    }

    private ProjectItem projectItem(Project project, CatalogItem catalogItem) {
        ProjectItem item = new ProjectItem();
        item.setProject(project);
        item.setCatalogItem(catalogItem);
        ProjectItemAllocation allocation = new ProjectItemAllocation();
        allocation.setProjectItem(item);
        allocation.setQuantity(BigDecimal.ONE);
        item.getAllocations().add(allocation);
        return item;
    }
}
