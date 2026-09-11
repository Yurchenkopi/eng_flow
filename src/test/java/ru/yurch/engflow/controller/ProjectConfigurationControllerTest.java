package ru.yurch.engflow.controller;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import ru.yurch.engflow.model.Project;
import ru.yurch.engflow.model.ProjectItem;
import ru.yurch.engflow.model.ProjectItemAllocation;
import ru.yurch.engflow.model.ProjectSubsection;
import ru.yurch.engflow.service.ProjectAssemblyService;
import ru.yurch.engflow.service.ProjectItemService;
import ru.yurch.engflow.service.ProjectService;
import ru.yurch.engflow.service.ProcurementProgress;
import ru.yurch.engflow.service.ProcurementService;
import ru.yurch.engflow.service.OrganizationService;
import ru.yurch.engflow.service.TransferActService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProjectConfigurationControllerTest {

    @Test
    void workshopTotalsIncludeOnlySubsectionAllocationsAndTransferredActs() {
        ProjectService projects = mock(ProjectService.class);
        ProjectItemService items = mock(ProjectItemService.class);
        ProjectAssemblyService assemblies = mock(ProjectAssemblyService.class);
        TransferActService transferActs = mock(TransferActService.class);
        ProcurementService procurements = mock(ProcurementService.class);
        OrganizationService organizations = mock(OrganizationService.class);
        ProjectConfigurationController controller = new ProjectConfigurationController(projects, items, assemblies, transferActs,procurements,organizations);

        Project project = new Project();
        project.setId(1L);
        ProjectItem item = new ProjectItem();
        item.setId(2L);
        item.getAllocations().add(allocation(10L, "2", false));
        item.getAllocations().add(allocation(11L, "7", true));
        item.getAllocations().add(allocation(12L, "1", true));

        when(projects.findById(1L)).thenReturn(project);
        when(assemblies.findByProject(1L)).thenReturn(List.of());
        when(items.search(1L, null, null, null, "name", "asc")).thenReturn(List.of(item));
        when(transferActs.transferredByProject(1L)).thenReturn(Map.of(
                10L, new BigDecimal("2"),
                11L, new BigDecimal("7")
        ));
        when(procurements.progressFor(List.of(item))).thenReturn(Map.of(2L,new ProcurementProgress(ru.yurch.engflow.model.ProcurementStatus.NOT_PLANNED,BigDecimal.ZERO,BigDecimal.ZERO,new BigDecimal("10"))));

        ExtendedModelMap model = new ExtendedModelMap();
        assertThat(controller.configuration(1L, null, null, null, null, "name", "asc", model))
                .isEqualTo("project-items/configuration");

        assertThat(value(model, "parentWorkshopTotal", 2L)).isEqualByComparingTo("8");
        assertThat(value(model, "parentTransferred", 2L)).isEqualByComparingTo("7");
        assertThat(value(model, "remaining", 11L)).isZero();
        assertThat(value(model, "remaining", 12L)).isEqualByComparingTo("1");
    }

    @Test
    void itemWithoutSubsectionsHasZeroWorkshopTotalForDashRendering() {
        ProjectService projects = mock(ProjectService.class);
        ProjectItemService items = mock(ProjectItemService.class);
        ProjectAssemblyService assemblies = mock(ProjectAssemblyService.class);
        TransferActService transferActs = mock(TransferActService.class);
        ProcurementService procurements = mock(ProcurementService.class);
        OrganizationService organizations = mock(OrganizationService.class);
        ProjectConfigurationController controller = new ProjectConfigurationController(projects, items, assemblies, transferActs,procurements,organizations);

        ProjectItem item = new ProjectItem();
        item.setId(3L);
        item.getAllocations().add(allocation(20L, "4", false));
        when(items.search(1L, null, null, null, "name", "asc")).thenReturn(List.of(item));
        when(assemblies.findByProject(1L)).thenReturn(List.of());
        when(transferActs.transferredByProject(1L)).thenReturn(Map.of());
        when(procurements.progressFor(List.of(item))).thenReturn(Map.of(3L,new ProcurementProgress(ru.yurch.engflow.model.ProcurementStatus.NOT_PLANNED,BigDecimal.ZERO,BigDecimal.ZERO,new BigDecimal("4"))));

        ExtendedModelMap model = new ExtendedModelMap();
        controller.configuration(1L, null, null, null, null, "name", "asc", model);

        assertThat(value(model, "parentWorkshopTotal", 3L)).isZero();
        assertThat(value(model, "parentTransferred", 3L)).isZero();
    }

    private ProjectItemAllocation allocation(Long id, String quantity, boolean withSubsection) {
        ProjectItemAllocation allocation = new ProjectItemAllocation();
        allocation.setId(id);
        allocation.setQuantity(new BigDecimal(quantity));
        if (withSubsection) {
            allocation.setProjectSubsection(new ProjectSubsection());
        }
        return allocation;
    }

    @SuppressWarnings("unchecked")
    private BigDecimal value(ExtendedModelMap model, String attribute, Long key) {
        return ((Map<Long, BigDecimal>) model.get(attribute)).get(key);
    }
}
