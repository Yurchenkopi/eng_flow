package ru.yurch.engflow.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "procurements")
public class Procurement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "project_id", nullable = false) private Project project;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "supplier_id", nullable = false) private Organization supplier;
    @Column(name="sequence_number",nullable=false) private Integer sequenceNumber;
    @Column(name = "rfq_sent_at") private Instant rfqSentAt;
    @Column(name = "deleted_at") private Instant deletedAt;
    @Column(columnDefinition = "text") private String notes;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @PrePersist void onCreate(){Instant now=Instant.now();createdAt=now;updatedAt=now;}
    @PreUpdate void onUpdate(){updatedAt=Instant.now();}
    public Long getId(){return id;} public void setId(Long value){id=value;}
    public Project getProject(){return project;} public void setProject(Project value){project=value;}
    public Organization getSupplier(){return supplier;} public void setSupplier(Organization value){supplier=value;}
    public Integer getSequenceNumber(){return sequenceNumber;} public void setSequenceNumber(Integer value){sequenceNumber=value;}
    @Transient public String getDisplayNumber(){if(sequenceNumber==null||createdAt==null||supplier==null)return "—";String source=supplier.getShortName()==null||supplier.getShortName().isBlank()?supplier.getName():supplier.getShortName();String safe=source==null?"supplier":source.trim().replaceAll("[^\\p{L}\\p{N}]+","_").replaceAll("^_+|_+$","");if(safe.isBlank())safe="supplier";return safe+"_"+String.format("%03d",sequenceNumber)+"_"+java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(java.time.ZoneId.systemDefault()).format(createdAt);}
    public Instant getRfqSentAt(){return rfqSentAt;} public void setRfqSentAt(Instant value){rfqSentAt=value;}
    public Instant getDeletedAt(){return deletedAt;} public void setDeletedAt(Instant value){deletedAt=value;}
    public String getNotes(){return notes;} public void setNotes(String value){notes=value;}
    public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
