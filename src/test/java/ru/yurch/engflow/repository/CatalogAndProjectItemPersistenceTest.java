package ru.yurch.engflow.repository;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.yurch.engflow.model.CatalogItem;
import ru.yurch.engflow.model.Project;
import ru.yurch.engflow.model.ProjectAssembly;
import ru.yurch.engflow.model.ProjectItem;
import ru.yurch.engflow.model.ProjectItemAllocation;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class CatalogAndProjectItemPersistenceTest {

    @Autowired
    CatalogItemRepository catalogItems;
    @Autowired
    ProjectRepository projects;
    @Autowired
    ProjectAssemblyRepository assemblies;
    @Autowired
    ProjectItemRepository projectItems;
    @Autowired
    MeasurementUnitRepository units;

    @Test
    void createsCatalogAssemblyAndFractionalProjectItems() {
        CatalogItem catalogItem = new CatalogItem();
        catalogItem.setName("Труба");
        catalogItem.setMeasurementUnit(units.findByName("м").orElseThrow());
        catalogItem = catalogItems.saveAndFlush(catalogItem);
        Project project = new Project();
        project.setDesignation("ИТ500.00.00.000");
        project.setName("Тестовая установка");
        project = projects.saveAndFlush(project);
        ProjectAssembly assembly = new ProjectAssembly();
        assembly.setProject(project);
        assembly.setName("Газовая система");
        assembly = assemblies.saveAndFlush(assembly);
        ProjectItem linked = item(project, catalogItem, new BigDecimal("1.2500"));
        linked.getAllocations().getFirst().setProjectAssembly(assembly);
        ProjectItemAllocation withoutAssembly = allocation(new BigDecimal("0.5000"));
        withoutAssembly.setProjectItem(linked);
        linked.getAllocations().add(withoutAssembly);
        linked = projectItems.saveAndFlush(linked);

        assertThat(linked.getRequiredQuantity()).isEqualByComparingTo("1.7500");
        assertThat(linked.getProject().getId()).isEqualTo(project.getId());
        assertThat(linked.getCatalogItem().getId()).isEqualTo(catalogItem.getId());
        assertThat(linked.getAllocations().get(1).getProjectAssembly()).isNull();
        catalogItems.flush();
        CatalogItem loaded = catalogItems.findAll(org.springframework.data.domain.Sort.by("name")).getFirst();
        assertThat(loaded.getUnit()).isEqualTo("м");
    }

    private ProjectItem item(Project project, CatalogItem catalogItem, BigDecimal quantity) {
        ProjectItem item = new ProjectItem();
        item.setProject(project);
        item.setCatalogItem(catalogItem);
        ProjectItemAllocation allocation = allocation(quantity);
        allocation.setProjectItem(item);
        item.getAllocations().add(allocation);
        return item;
    }

    private ProjectItemAllocation allocation(BigDecimal quantity) {
        ProjectItemAllocation value = new ProjectItemAllocation();
        value.setQuantity(quantity);
        return value;
    }
}
