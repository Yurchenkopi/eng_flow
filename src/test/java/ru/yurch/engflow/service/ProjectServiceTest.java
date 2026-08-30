package ru.yurch.engflow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yurch.engflow.model.Project;
import ru.yurch.engflow.model.ProjectStatus;
import ru.yurch.engflow.repository.ProjectRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository);
    }

    @Test
    void createsProject() {
        Project project = project("  ИТ319.00.00.000  ");
        when(projectRepository.existsByDesignation("ИТ319.00.00.000")).thenReturn(false);
        when(projectRepository.save(project)).thenAnswer(invocation -> invocation.getArgument(0));

        Project savedProject = projectService.create(project);

        assertThat(savedProject.getDesignation()).isEqualTo("ИТ319.00.00.000");
        assertThat(savedProject.getStatus()).isEqualTo(ProjectStatus.DESIGN);
        assertThat(savedProject.getQuantity()).isEqualTo(1);
        verify(projectRepository).save(project);
    }

    @Test
    void rejectsDuplicateDesignation() {
        Project project = project("ИТ319.00.00.000");
        when(projectRepository.existsByDesignation("ИТ319.00.00.000")).thenReturn(true);

        assertThatThrownBy(() -> projectService.create(project))
                .isInstanceOf(DuplicateProjectDesignationException.class)
                .hasMessageContaining("ИТ319.00.00.000");
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void returnsProjectsOrderedByDesignation() {
        Project first = project("ИТ100.00.00.000");
        Project second = project("ИТ200.00.00.000");
        when(projectRepository.findAllByOrderByDesignationAsc()).thenReturn(List.of(first, second));

        assertThat(projectService.findAll()).containsExactly(first, second);
    }

    private Project project(String designation) {
        Project project = new Project();
        project.setDesignation(designation);
        project.setName("Установка вакуумного напыления");
        return project;
    }
}
