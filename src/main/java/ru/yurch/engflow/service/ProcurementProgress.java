package ru.yurch.engflow.service;

import ru.yurch.engflow.model.ProcurementStatus;
import java.math.BigDecimal;

public record ProcurementProgress(ProcurementStatus status,BigDecimal plannedQuantity,BigDecimal requestedQuantity,BigDecimal orderedQuantity,BigDecimal requiredQuantity) {
    public ProcurementProgress(ProcurementStatus status,BigDecimal requestedQuantity,BigDecimal orderedQuantity,BigDecimal requiredQuantity){this(status,requestedQuantity,requestedQuantity,orderedQuantity,requiredQuantity);}
    public String getDisplay(){
        if(status==ProcurementStatus.NOT_PLANNED)return "—";
        if(status==ProcurementStatus.PLANNED)return "В закупке: "+plannedQuantity.stripTrailingZeros().toPlainString()+" / "+requiredQuantity.stripTrailingZeros().toPlainString();
        if(status==ProcurementStatus.PARTIALLY_ORDERED||status==ProcurementStatus.ORDERED)return "Заказано: "+orderedQuantity.stripTrailingZeros().toPlainString()+" / "+requiredQuantity.stripTrailingZeros().toPlainString();
        return "Запрошено: "+requestedQuantity.stripTrailingZeros().toPlainString()+" / "+requiredQuantity.stripTrailingZeros().toPlainString();
    }
    public String getCssClass(){return switch(status){case PLANNED->"procurement-planned";case PARTIALLY_REQUESTED,REQUESTED->"procurement-requested";case PARTIALLY_ORDERED,ORDERED->"procurement-order";case PARTIALLY_RECEIVED->"procurement-partial-received";case RECEIVED,IN_STOCK->"procurement-received";default->"";};}
}
