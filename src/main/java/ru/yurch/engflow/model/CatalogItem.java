package ru.yurch.engflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.LinkedHashSet; import java.util.Set;

@Entity
@Table(name = "catalog_items")
public class CatalogItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Size(max = 255) private String designation;
    @NotBlank(message = "Укажите наименование изделия") @Size(max = 255)
    @Column(nullable = false) private String name;
    @Size(max = 255) private String manufacturer;
    @NotNull(message="Укажите единицу измерения") @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="measurement_unit_id",nullable=false) private MeasurementUnit measurementUnit = new MeasurementUnit();
    @Column(columnDefinition = "text") private String notes;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @OneToMany(mappedBy="catalogItem") @OrderBy("id ASC") private Set<ItemSupplier> itemSuppliers=new LinkedHashSet<>();

    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getDesignation() { return designation; } public void setDesignation(String designation) { this.designation = designation; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getManufacturer() { return manufacturer; } public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public MeasurementUnit getMeasurementUnit(){return measurementUnit;} public void setMeasurementUnit(MeasurementUnit value){measurementUnit=value;}
    @Transient public String getUnit(){return measurementUnit==null?"":measurementUnit.getName();}
    public String getNotes() { return notes; } public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; } public Instant getUpdatedAt() { return updatedAt; }
    public Set<ItemSupplier> getItemSuppliers(){return itemSuppliers;}
    @Transient public String getSupplierSummary(){int count=itemSuppliers.size();if(count==0)return "—";if(count==1){Organization supplier=itemSuppliers.iterator().next().getSupplier();return supplier.getShortName()==null||supplier.getShortName().isBlank()?supplier.getName():supplier.getShortName();}int mod100=count%100,mod10=count%10;String word=mod100>=11&&mod100<=14?"поставщиков":mod10==1?"поставщик":mod10>=2&&mod10<=4?"поставщика":"поставщиков";return count+" "+word;}
    @Transient public BigDecimal getQuantityStep() { String value=getUnit().trim().toLowerCase(java.util.Locale.ROOT); return "шт.".equals(value)?BigDecimal.ONE:("кг".equals(value)||"м".equals(value)?new BigDecimal("0.1"):new BigDecimal("0.0001")); }
}
