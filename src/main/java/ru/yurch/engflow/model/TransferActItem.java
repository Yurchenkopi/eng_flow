package ru.yurch.engflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity @Table(name="transfer_act_items")
public class TransferActItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="transfer_act_id",nullable=false) private TransferAct transferAct;
    @NotNull @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="project_item_id",nullable=false) private ProjectItem projectItem;
    @Column(name="destination_designation") private String destinationDesignation;
    @Column(name="shop_number",length=100) private String shopNumber;
    @NotNull(message="Укажите количество") @DecimalMin(value="0.0001",message="Количество должно быть положительным") @Digits(integer=15,fraction=4)
    @Column(nullable=false,precision=19,scale=4) private BigDecimal quantity;
    @Column(columnDefinition="text") private String notes;
    @Transient private BigDecimal totalSameCatalogItem;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public TransferAct getTransferAct(){return transferAct;} public void setTransferAct(TransferAct transferAct){this.transferAct=transferAct;}
    public ProjectItem getProjectItem(){return projectItem;} public void setProjectItem(ProjectItem projectItem){this.projectItem=projectItem;}
    public String getDestinationDesignation(){return destinationDesignation;} public void setDestinationDesignation(String value){destinationDesignation=value;}
    public String getShopNumber(){return shopNumber;} public void setShopNumber(String value){shopNumber=value;}
    public BigDecimal getQuantity(){return quantity;} public void setQuantity(BigDecimal value){quantity=value;}
    public String getNotes(){return notes;} public void setNotes(String value){notes=value;}
    public BigDecimal getTotalSameCatalogItem(){return totalSameCatalogItem;} public void setTotalSameCatalogItem(BigDecimal value){totalSameCatalogItem=value;}
}
