package ru.yurch.engflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "organizations")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Укажите наименование организации")
    @Size(max = 255, message = "Наименование должно содержать не более 255 символов")
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 100, message = "Краткое наименование должно содержать не более 100 символов")
    @Column(name = "short_name", length = 100)
    private String shortName;

    @Size(max = 12, message = "ИНН должен содержать не более 12 символов")
    @Column(name = "inn", length = 12)
    private String inn;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
