package ru.yurch.engflow.model;

public enum OrganizationRole {
    CUSTOMER("Заказчик"), SUPPLIER("Поставщик");
    private final String displayName;
    OrganizationRole(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
