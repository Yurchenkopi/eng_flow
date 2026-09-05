package ru.yurch.engflow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.model.*;
import ru.yurch.engflow.repository.*;
import ru.yurch.engflow.controller.ProjectItemController;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE) @ActiveProfiles("test") @Transactional
class CatalogAndConfigurationServiceTest {
    @Autowired CatalogItemService catalogService; @Autowired ProjectItemService projectItemService;
    @Autowired ProjectRepository projects; @Autowired ProjectItemRepository projectItems; @Autowired ProjectAssemblyRepository assemblies; @Autowired ProjectItemController projectItemController;
    private CatalogItem valve; private CatalogItem tube;
    @BeforeEach void setUp() {
        valve = item("VAT-10", "Клапан", "VAT"); tube = item("DIN-20", "Труба", "Example");
        valve = catalogService.create(valve); tube = catalogService.create(tube);
    }
    @Test void searchesByDesignationNameAndIgnoresCase() {
        assertThat(catalogService.autocomplete("vat-10")).extracting(CatalogItem::getId).contains(valve.getId());
        assertThat(catalogService.autocomplete("КЛАПАН")).extracting(CatalogItem::getId).contains(valve.getId());
        assertThat(catalogService.autocomplete("труба")).extracting(CatalogItem::getId).contains(tube.getId());
    }
    @Test void putsExactDesignationFirst() {
        catalogService.create(item("X-VAT-10-X", "Другой клапан", "VAT"));
        assertThat(catalogService.autocomplete("vat-10").getFirst().getId()).isEqualTo(valve.getId());
    }
    @Test void copyCreatesNewItemWithoutChangingSource() {
        CatalogItem copy = catalogService.prepareCopy(valve.getId()); copy.setName("Клапан, вариант"); CatalogItem saved = catalogService.create(copy);
        assertThat(saved.getId()).isNotEqualTo(valve.getId()); assertThat(catalogService.findById(valve.getId()).getName()).isEqualTo("Клапан");
    }
    @Test void configurationFilterDoesNotMixProjects() {
        Project first = project("ИТ701.00.00.000"), second = project("ИТ702.00.00.000");
        projectItems.save(projectItem(first, valve)); projectItems.save(projectItem(second, tube));
        assertThat(projectItemService.search(first.getId(), "", null, "designation", "asc")).allMatch(item -> item.getProject().getId().equals(first.getId())).extracting(item -> item.getCatalogItem().getId()).containsExactly(valve.getId());
    }
    @Test void projectItemIsUniquePerProjectAndCatalogAndQuantityIsAllocationSum() {
        Project project=project("ИТ703.00.00.000"); ProjectItem first=projectItem(project,valve);
        ProjectItemAllocation second=new ProjectItemAllocation();second.setQuantity(BigDecimal.ONE);second.setProjectAssembly(assembly(project,"Камера"));first.getAllocations().add(second);
        ProjectItem saved=projectItemService.create(project.getId(),first);
        assertThat(saved.getRequiredQuantity()).isEqualByComparingTo("2.0");
        assertThatThrownBy(()->projectItemService.create(project.getId(),projectItem(project,valve))).isInstanceOf(ProjectItemService.DuplicateProjectItemException.class);
    }
    @Test void configurationSearchReturnsUniqueItemsAndLoadsEveryAllocation() {
        Project project=project("ИТ704.00.00.000");ProjectAssembly chamber=assembly(project,"Вакуумная камера");ProjectAssembly cooling=assembly(project,"Охлаждение");ProjectItem item=projectItem(project,valve);item.getAllocations().getFirst().setProjectAssembly(chamber);ProjectItemAllocation second=new ProjectItemAllocation();second.setProjectItem(item);second.setProjectAssembly(cooling);second.setQuantity(new BigDecimal("2.5"));item.getAllocations().add(second);projectItems.saveAndFlush(item);
        assertSearchHasOneItemWithBothAllocations(project,"",null);
        assertSearchHasOneItemWithBothAllocations(project,"",chamber.getId());
        assertSearchHasOneItemWithBothAllocations(project,"vat",null);
        assertSearchHasOneItemWithBothAllocations(project,"вакуумная",null);
    }
    @Test void rejectsRepeatedAssemblyIncludingWithoutAssembly() {
        Project project=project("ИТ705.00.00.000");ProjectItem noAssembly=projectItem(project,valve);ProjectItemAllocation duplicate=new ProjectItemAllocation();duplicate.setQuantity(BigDecimal.ONE);noAssembly.getAllocations().add(duplicate);
        assertThatThrownBy(()->projectItemService.create(project.getId(),noAssembly)).hasMessageContaining("Без раздела");
        ProjectAssembly chamber=assembly(project,"Камера");ProjectItem sameAssembly=projectItem(project,tube);sameAssembly.getAllocations().getFirst().setProjectAssembly(chamber);ProjectItemAllocation repeated=new ProjectItemAllocation();repeated.setProjectAssembly(chamber);repeated.setQuantity(BigDecimal.ONE);sameAssembly.getAllocations().add(repeated);
        assertThatThrownBy(()->projectItemService.create(project.getId(),sameAssembly)).hasMessageContaining("один раз");
    }
    @Test void quantityStepDependsOnCatalogUnit() {
        valve.setUnit("шт.");assertThat(valve.getQuantityStep()).isEqualByComparingTo("1.0");
        valve.setUnit("кг");assertThat(valve.getQuantityStep()).isEqualByComparingTo("0.1");
        valve.setUnit("уп.");assertThat(valve.getQuantityStep()).isEqualByComparingTo("0.0001");
    }
    @Test void backendAppliesQuantityStepForPiecesKilogramsAndMeters(){Project pieces=project("ИТ708.00.00.000");ProjectItem invalid=projectItem(pieces,valve);invalid.getAllocations().getFirst().setQuantity(new BigDecimal("1.5"));assertThatThrownBy(()->projectItemService.create(pieces.getId(),invalid)).hasMessageContaining("шагу");CatalogItem kilograms=item("KG-1","Материал","Example");kilograms.setUnit("кг");kilograms=catalogService.create(kilograms);Project kgProject=project("ИТ709.00.00.000");ProjectItem kg=projectItem(kgProject,kilograms);kg.getAllocations().getFirst().setQuantity(new BigDecimal("1.2"));assertThat(projectItemService.create(kgProject.getId(),kg).getRequiredQuantity()).isEqualByComparingTo("1.2");CatalogItem meters=item("M-1","Труба","Example");meters.setUnit("м");meters=catalogService.create(meters);Project mProject=project("ИТ710.00.00.000");ProjectItem m=projectItem(mProject,meters);m.getAllocations().getFirst().setQuantity(new BigDecimal("0.3"));assertThat(projectItemService.create(mProject.getId(),m).getRequiredQuantity()).isEqualByComparingTo("0.3");}
    @Test void sectionSummaryUsesNameAndRussianCount(){Project project=project("ИТ706.00.00.000");ProjectAssembly chamber=assembly(project,"Камера");ProjectItem value=projectItem(project,valve);value.getAllocations().getFirst().setProjectAssembly(chamber);assertThat(value.getSectionSummary()).isEqualTo("Камера");ProjectItemAllocation second=new ProjectItemAllocation();second.setQuantity(BigDecimal.ONE);value.getAllocations().add(second);assertThat(value.getSectionSummary()).isEqualTo("2 раздела");}
    @Test void existingCatalogSelectionReturnsImmediateEditUrl(){Project project=project("ИТ707.00.00.000");ProjectItem saved=projectItemService.create(project.getId(),projectItem(project,valve));assertThat(projectItemController.existing(project.getId(),valve.getId())).containsEntry("exists",true).containsEntry("url","/projects/"+project.getId()+"/items/"+saved.getId()+"/edit?alreadyExists=1");}
    private CatalogItem item(String designation, String name, String manufacturer) { CatalogItem item = new CatalogItem(); item.setDesignation(designation); item.setName(name); item.setManufacturer(manufacturer); item.setUnit("шт."); return item; }
    private Project project(String designation) { Project project = new Project(); project.setDesignation(designation); project.setName("Проект"); return projects.save(project); }
    private ProjectAssembly assembly(Project project,String name){ProjectAssembly value=new ProjectAssembly();value.setProject(project);value.setName(name);return assemblies.save(value);}
    private void assertSearchHasOneItemWithBothAllocations(Project project,String query,Long assemblyId){var result=projectItemService.search(project.getId(),query,assemblyId,"name","asc");assertThat(result).hasSize(1);assertThat(result.getFirst().getAllocations()).hasSize(2);}
    private ProjectItem projectItem(Project project, CatalogItem catalogItem) { ProjectItem item = new ProjectItem(); item.setProject(project); item.setCatalogItem(catalogItem); ProjectItemAllocation allocation=new ProjectItemAllocation();allocation.setProjectItem(item);allocation.setQuantity(BigDecimal.ONE);item.getAllocations().add(allocation);return item; }
}
