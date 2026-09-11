package ru.yurch.engflow.controller;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import ru.yurch.engflow.model.*;
import ru.yurch.engflow.service.*;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProcurementControllerTest {
    @Test void firstSubmitOnlyBuildsDraftAndDoesNotCreateProcurement(){ProcurementService service=mock(ProcurementService.class);ProjectItem item=new ProjectItem();item.setId(7L);ProjectItemAllocation allocation=new ProjectItemAllocation();allocation.setQuantity(new BigDecimal("3"));item.getAllocations().add(allocation);CatalogItem catalog=new CatalogItem();catalog.setName("Патрубок");item.setCatalogItem(catalog);when(service.selectedForConfiguration(1L,22L,List.of(7L),false)).thenReturn(List.of(item));when(service.progress(item)).thenReturn(new ProcurementProgress(ProcurementStatus.NOT_PLANNED,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,new BigDecimal("3")));OrganizationService organizations=mock(OrganizationService.class);Organization supplier=new Organization();supplier.setId(22L);supplier.setName("Поставщик");when(organizations.findById(22L)).thenReturn(supplier);ProcurementController controller=new ProcurementController(service,mock(ProjectService.class),mock(ProjectItemService.class),organizations);String view=controller.createFromConfiguration(1L,22L,List.of(7L),null,false,new ExtendedModelMap(),new RedirectAttributesModelMap());assertThat(view).isEqualTo("procurements/configuration-draft");verify(service,never()).createFromConfiguration(any(),any(),anyList(),anyList(),anyBoolean());}

    @Test void confirmationCreatesExactlyOneProcurement(){ProcurementService service=mock(ProcurementService.class);Procurement saved=new Procurement();saved.setId(31L);when(service.createFromConfiguration(1L,22L,List.of(7L,8L),List.of(new BigDecimal("2"),new BigDecimal("1")),false)).thenReturn(saved);String view=controller(service).createFromConfiguration(1L,22L,List.of(7L,8L),List.of(new BigDecimal("2"),new BigDecimal("1")),false,new ExtendedModelMap(),new RedirectAttributesModelMap());assertThat(view).isEqualTo("redirect:/projects/1/procurements/31");verify(service).createFromConfiguration(1L,22L,List.of(7L,8L),List.of(new BigDecimal("2"),new BigDecimal("1")),false);}

    @Test void selectedProcurementsAreDeleted(){ProcurementService service=mock(ProcurementService.class);RedirectAttributesModelMap redirect=new RedirectAttributesModelMap();String view=controller(service).deleteSelected(1L,List.of(17L,18L),redirect);assertThat(view).isEqualTo("redirect:/projects/1/procurements");assertThat(redirect.getFlashAttributes().get("successMessage")).isEqualTo("Удалено закупок: 2");verify(service).delete(1L,17L);verify(service).delete(1L,18L);}

    @Test void emptyProcurementSelectionIsRejected(){ProcurementService service=mock(ProcurementService.class);RedirectAttributesModelMap redirect=new RedirectAttributesModelMap();String view=controller(service).deleteSelected(1L,null,redirect);assertThat(view).isEqualTo("redirect:/projects/1/procurements");assertThat(redirect.getFlashAttributes().get("errorMessage")).isEqualTo("Выберите закупки для удаления");verify(service,never()).delete(anyLong(),anyLong());}

    private ProcurementController controller(ProcurementService service){return new ProcurementController(service,mock(ProjectService.class),mock(ProjectItemService.class),mock(OrganizationService.class));}
}
