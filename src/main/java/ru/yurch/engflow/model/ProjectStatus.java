package ru.yurch.engflow.model;

public enum ProjectStatus {
    DESIGN("Проектирование"),
    PRODUCTION("Производство"),
    COMPLETED("Завершен");

    private final String displayName;

    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
