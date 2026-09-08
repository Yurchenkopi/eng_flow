package ru.yurch.engflow.controller;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yurch.engflow.model.*;
import ru.yurch.engflow.repository.ProjectItemAllocationRepository;
import ru.yurch.engflow.service.TransferActService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class TransferActControllerMvcTest {
 private TransferActService service;private MockMvc mvc;
 @BeforeEach void setUp(){service=mock(TransferActService.class);mvc=MockMvcBuilders.standaloneSetup(new TransferActController(service,mock(ProjectItemAllocationRepository.class))).build();}
 @Test void createFormHasTodayInBackingObject() throws Exception {TransferAct act=formAct(LocalDate.now());when(service.prepare(1L,List.of(2L))).thenReturn(act);when(service.remaining(any())).thenReturn(BigDecimal.ONE);mvc.perform(get("/transfer-acts/new").param("projectId","1").param("allocationIds","2")).andExpect(status().isOk()).andExpect(view().name("transfer-acts/form")).andExpect(model().attribute("transferAct",hasProperty("actDate",is(LocalDate.now()))));}
 @Test void editFormKeepsSavedDraftDate() throws Exception {LocalDate saved=LocalDate.of(2024,4,15);TransferAct act=formAct(saved);act.setId(9L);when(service.findById(9L)).thenReturn(act);when(service.remaining(any())).thenReturn(BigDecimal.ONE);mvc.perform(get("/transfer-acts/9/edit")).andExpect(status().isOk()).andExpect(model().attribute("transferAct",hasProperty("actDate",is(saved))));}
 private TransferAct formAct(LocalDate date){Project project=new Project();project.setId(1L);project.setDesignation("ИТ1");project.setName("Проект");CatalogItem catalog=new CatalogItem();catalog.setName("Изделие");ProjectItem item=new ProjectItem();item.setCatalogItem(catalog);ProjectItemAllocation allocation=new ProjectItemAllocation();allocation.setId(2L);allocation.setProjectItem(item);allocation.setQuantity(BigDecimal.ONE);TransferActItem line=new TransferActItem();line.setProjectItemAllocation(allocation);line.setQuantity(BigDecimal.ONE);TransferAct act=new TransferAct();act.setActDate(date);act.setProject(project);act.getItems().add(line);return act;}
}
