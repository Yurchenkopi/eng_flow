package ru.yurch.engflow.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.yurch.engflow.model.*;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class CatalogAndProjectItemPersistenceTest {
    @Autowired CatalogItemRepository catalogItems;
    @Autowired ProjectRepository projects;
    @Autowired ProjectAssemblyRepository assemblies;
    @Autowired ProjectItemRepository projectItems;

    @Test
    void createsCatalogAssemblyAndFractionalProjectItems() {
        CatalogItem catalogItem = new CatalogItem(); catalogItem.setName("Труба"); catalogItem.setUnit("м");
        catalogItem = catalogItems.saveAndFlush(catalogItem);
        Project project = new Project(); project.setDesignation("ИТ500.00.00.000"); project.setName("Тестовая установка");
        project = projects.saveAndFlush(project);
        ProjectAssembly assembly = new ProjectAssembly(); assembly.setProject(project); assembly.setName("Газовая система");
        assembly = assemblies.saveAndFlush(assembly);
        ProjectItem linked = item(project, catalogItem, new BigDecimal("1.2500")); linked.setProjectAssembly(assembly);
        linked = projectItems.saveAndFlush(linked);
        ProjectItem withoutAssembly = projectItems.saveAndFlush(item(project, catalogItem, new BigDecimal("0.5000")));

        assertThat(linked.getRequiredQuantity()).isEqualByComparingTo("1.2500");
        assertThat(linked.getProject().getId()).isEqualTo(project.getId());
        assertThat(linked.getCatalogItem().getId()).isEqualTo(catalogItem.getId());
        assertThat(withoutAssembly.getProjectAssembly()).isNull();
    }

    private ProjectItem item(Project project, CatalogItem catalogItem, BigDecimal quantity) {
        ProjectItem item = new ProjectItem(); item.setProject(project); item.setCatalogItem(catalogItem); item.setRequiredQuantity(quantity); return item;
    }
}
