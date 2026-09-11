package ru.yurch.engflow.dto;

import java.util.ArrayList;
import java.util.List;

public class AnalogReplacementForm {
    private Long targetCatalogItemId;
    private List<AnalogReplacementRow> rows=new ArrayList<>();
    public Long getTargetCatalogItemId(){return targetCatalogItemId;} public void setTargetCatalogItemId(Long value){targetCatalogItemId=value;}
    public List<AnalogReplacementRow> getRows(){return rows;} public void setRows(List<AnalogReplacementRow> value){rows=value==null?new ArrayList<>():value;}
}
