package ru.yurch.engflow.controller;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateStructureTest {

    @Test
    void catalogAndProjectItemFormsUseFullContentWidth() throws IOException {
        assertThat(resource("templates/catalog-items/form.html")).contains("form-card-full");
        assertThat(resource("templates/project-items/form.html")).contains("form-card-full");
    }

    @Test
    void transferDetailCheckboxHasAssociatedLabel() throws IOException {
        String template = resource("templates/project-items/form.html");

        assertThat(template)
                .contains("th:id=\"${'allocation-detail-'+stat.index}\"")
                .contains("th:for=\"${'allocation-detail-'+stat.index}\"")
                .contains("Детализировать для передачи в цех");
    }

    private String resource(String path) throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Resource not found: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
