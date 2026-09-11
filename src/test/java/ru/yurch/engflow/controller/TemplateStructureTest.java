package ru.yurch.engflow.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

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

    @Test
    void procurementListSupportsMultiSelectionDeletion() throws IOException {
        assertThat(resource("templates/procurements/list.html"))
                .contains("id=\"procurement-delete-selection\"")
                .contains("class=\"form-check-input procurement-delete-check\"")
                .contains("id=\"delete-procurements\"")
                .contains("delete-selected")
                .contains("disabled>Удалить");
    }

    @Test
    void procurementRowColorsOverrideBootstrapTablePainting() throws IOException {
        assertThat(resource("static/css/app.css"))
                .contains(".configuration-table .procurement-planned > td")
                .contains("--bs-table-bg:#f5f3ed")
                .contains(".configuration-table .procurement-requested > td")
                .contains("--bs-table-bg:#fff9e8")
                .contains(".configuration-table .procurement-order > td")
                .contains("--bs-table-bg:#e5f1ff")
                .contains("box-shadow:none!important");
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
