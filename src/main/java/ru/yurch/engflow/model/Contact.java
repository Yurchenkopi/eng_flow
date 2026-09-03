package ru.yurch.engflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity @Table(name = "contacts")
public class Contact {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organization_id", nullable = false) private Organization organization;
    @NotBlank(message = "Укажите ФИО") @Size(max = 255) @Column(name = "full_name", nullable = false) private String fullName;
    @Size(max = 255) private String position;
    @Email(message = "Некорректный email") @Size(max = 255) private String email;
    @Size(max = 100) private String phone;
    @Column(name = "is_primary", nullable = false) private boolean primary;
    @Column(columnDefinition = "text") private String notes;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Organization getOrganization(){return organization;} public void setOrganization(Organization organization){this.organization=organization;}
    public String getFullName(){return fullName;} public void setFullName(String fullName){this.fullName=fullName;}
    public String getPosition(){return position;} public void setPosition(String position){this.position=position;}
    public String getEmail(){return email;} public void setEmail(String email){this.email=email;}
    public String getPhone(){return phone;} public void setPhone(String phone){this.phone=phone;}
    public boolean isPrimary(){return primary;} public void setPrimary(boolean primary){this.primary=primary;}
    public String getNotes(){return notes;} public void setNotes(String notes){this.notes=notes;}
}
