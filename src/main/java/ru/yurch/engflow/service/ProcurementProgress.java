package ru.yurch.engflow.service;

import ru.yurch.engflow.model.ProcurementStatus;
import java.math.BigDecimal;

public record ProcurementProgress(ProcurementStatus status,BigDecimal requestedQuantity,BigDecimal orderedQuantity,BigDecimal requiredQuantity) {
    public String getDisplay(){
        if(status==ProcurementStatus.NOT_REQUESTED)return "—";
        if(status==ProcurementStatus.RFQ_SENT)return status.getDisplayName();
        return orderedQuantity.stripTrailingZeros().toPlainString()+" / "+requiredQuantity.stripTrailingZeros().toPlainString()+" заказано";
    }
    public boolean isPartialOrder(){return status==ProcurementStatus.ORDER_PLACED&&orderedQuantity.compareTo(requiredQuantity)<0;}
    public String getCssClass(){return switch(status){case RFQ_SENT->"procurement-rfq";case ORDER_PLACED->isPartialOrder()?"procurement-order-partial":"procurement-order";case PARTIALLY_RECEIVED->"procurement-partial-received";case RECEIVED,IN_STOCK->"procurement-received";default->"";};}
}
