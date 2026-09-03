package ru.yurch.engflow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.model.*;
import ru.yurch.engflow.repository.*;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE) @ActiveProfiles("test") @Transactional
class CatalogAndConfigurationServiceTest {
    @Autowired CatalogItemService catalogService; @Autowired ProjectItemService projectItemService;
    @Autowired ProjectRepository projects; @Autowired ProjectItemRepository projectItems;
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
    private CatalogItem item(String designation, String name, String manufacturer) { CatalogItem item = new CatalogItem(); item.setDesignation(designation); item.setName(name); item.setManufacturer(manufacturer); item.setUnit("шт."); return item; }
    private Project project(String designation) { Project project = new Project(); project.setDesignation(designation); project.setName("Проект"); return projects.save(project); }
    private ProjectItem projectItem(Project project, CatalogItem catalogItem) { ProjectItem item = new ProjectItem(); item.setProject(project); item.setCatalogItem(catalogItem); item.setRequiredQuantity(new BigDecimal("1.5")); return item; }
}
