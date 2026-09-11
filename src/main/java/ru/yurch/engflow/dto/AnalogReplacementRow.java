package ru.yurch.engflow.dto;

import java.math.BigDecimal;

public class AnalogReplacementRow {

    private Long sourceAllocationId;
    private BigDecimal originalQuantity;
    private BigDecimal replacementQuantity;

    public Long getSourceAllocationId() {
        return sourceAllocationId;
    }

    public void setSourceAllocationId(Long value) {
        sourceAllocationId = value;
    }

    public BigDecimal getOriginalQuantity() {
        return originalQuantity;
    }

    public void setOriginalQuantity(BigDecimal value) {
        originalQuantity = value;
    }

    public BigDecimal getReplacementQuantity() {
        return replacementQuantity;
    }

    public void setReplacementQuantity(BigDecimal value) {
        replacementQuantity = value;
    }
}
