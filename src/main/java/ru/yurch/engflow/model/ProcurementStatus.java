package ru.yurch.engflow.model;

public enum ProcurementStatus {
    NOT_REQUESTED("Не запрошено"),
    RFQ_SENT("Запрос отправлен"),
    ORDER_PLACED("Заказ размещен"),
    PARTIALLY_RECEIVED("Частично получено"),
    RECEIVED("Получено"),
    IN_STOCK("В наличии");

    private final String displayName;

    ProcurementStatus(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
