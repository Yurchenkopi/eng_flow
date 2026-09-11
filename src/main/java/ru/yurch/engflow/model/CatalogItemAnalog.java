package ru.yurch.engflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
        name = "catalog_item_analogs",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_catalog_item_analogs_pair",
                columnNames = {
                        "first_catalog_item_id",
                        "second_catalog_item_id"}))
public class CatalogItemAnalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "first_catalog_item_id", nullable = false)
    private CatalogItem firstCatalogItem;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "second_catalog_item_id", nullable = false)
    private CatalogItem secondCatalogItem;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public CatalogItem getFirstCatalogItem() {
        return firstCatalogItem;
    }

    public void setFirstCatalogItem(CatalogItem value) {
        firstCatalogItem = value;
    }

    public CatalogItem getSecondCatalogItem() {
        return secondCatalogItem;
    }

    public void setSecondCatalogItem(CatalogItem value) {
        secondCatalogItem = value;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public CatalogItem other(Long catalogItemId) {
        return firstCatalogItem.getId().equals(catalogItemId) ? secondCatalogItem : firstCatalogItem;
    }
}
