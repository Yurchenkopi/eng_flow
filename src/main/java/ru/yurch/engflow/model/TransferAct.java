package ru.yurch.engflow.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="transfer_acts", uniqueConstraints=@UniqueConstraint(name="uk_transfer_acts_year_number", columnNames={"act_year","number"}))
public class TransferAct {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private Integer number;
    @Column(name="act_year",nullable=false) private Integer year;
    @NotNull(message="Укажите дату акта") @Column(name="act_date",nullable=false) private LocalDate actDate=LocalDate.now();
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="project_id",nullable=false) private Project project;
    @NotBlank(message="Укажите, кто сдал") @Column(name="delivered_by",nullable=false) private String deliveredBy;
    @NotBlank(message="Укажите, кто принял") @Column(name="received_by",nullable=false) private String receivedBy;
    @Column(columnDefinition="text") private String notes;
    @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @OneToMany(mappedBy="transferAct",cascade=CascadeType.ALL,orphanRemoval=true) private List<@Valid TransferActItem> items=new ArrayList<>();
    @PrePersist void onCreate(){createdAt=Instant.now();}
    public String getDisplayNumber(){return number+"/"+year;}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Integer getNumber(){return number;} public void setNumber(Integer number){this.number=number;}
    public Integer getYear(){return year;} public void setYear(Integer year){this.year=year;}
    public LocalDate getActDate(){return actDate;} public void setActDate(LocalDate actDate){this.actDate=actDate;}
    public Project getProject(){return project;} public void setProject(Project project){this.project=project;}
    public String getDeliveredBy(){return deliveredBy;} public void setDeliveredBy(String deliveredBy){this.deliveredBy=deliveredBy;}
    public String getReceivedBy(){return receivedBy;} public void setReceivedBy(String receivedBy){this.receivedBy=receivedBy;}
    public String getNotes(){return notes;} public void setNotes(String notes){this.notes=notes;}
    public Instant getCreatedAt(){return createdAt;}
    public List<TransferActItem> getItems(){return items;} public void setItems(List<TransferActItem> items){this.items=items==null?new ArrayList<>():items;}
}
