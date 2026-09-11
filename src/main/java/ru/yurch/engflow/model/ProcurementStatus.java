package ru.yurch.engflow.model;

public enum ProcurementStatus {

    NOT_PLANNED("Не запланировано"), PLANNED("В закупке"), PARTIALLY_REQUESTED("Частично запрошено"), REQUESTED(
            "Запрошено"), PARTIALLY_ORDERED("Частично заказано"), ORDERED(
                    "Заказано"), PARTIALLY_RECEIVED("Частично получено"), RECEIVED("Получено"), IN_STOCK("В наличии");

    private final String displayName;

    ProcurementStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
