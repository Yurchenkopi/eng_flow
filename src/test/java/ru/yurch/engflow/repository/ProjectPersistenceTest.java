package ru.yurch.engflow.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import ru.yurch.engflow.model.Project;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class ProjectPersistenceTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void databaseRejectsDuplicateDesignation() {
        projectRepository.saveAndFlush(project("ИТ319.00.00.000", "Первая установка"));

        assertThatThrownBy(() -> projectRepository.saveAndFlush(
                project("ИТ319.00.00.000", "Вторая установка")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Project project(String designation, String name) {
        Project project = new Project();
        project.setDesignation(designation);
        project.setName(name);
        return project;
    }
}
