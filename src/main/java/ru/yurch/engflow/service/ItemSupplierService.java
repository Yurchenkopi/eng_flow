package ru.yurch.engflow.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.model.CatalogItem;
import ru.yurch.engflow.model.ItemSupplier;
import ru.yurch.engflow.model.Organization;
import ru.yurch.engflow.model.OrganizationRole;
import ru.yurch.engflow.repository.ItemSupplierRepository;
import ru.yurch.engflow.repository.OrganizationRepository;

@Service
@Transactional(readOnly = true)
public class ItemSupplierService {

    private final ItemSupplierRepository repository;
    private final CatalogItemService catalogItems;
    private final OrganizationRepository organizations;

    public ItemSupplierService(ItemSupplierRepository repository, CatalogItemService catalogItems, OrganizationRepository organizations) {
        this.repository = repository;
        this.catalogItems = catalogItems;
        this.organizations = organizations;
    }

    public List<ItemSupplier> findByCatalogItem(Long id) {
        return repository.findByCatalogItemIdOrderBySupplierNameAsc(id);
    }

    public ItemSupplier find(Long catalogItemId, Long id) {
        return repository.findById(id).filter(value -> value.getCatalogItem().getId().equals(catalogItemId))
                .orElseThrow(() -> new IllegalArgumentException("Поставщик изделия не найден"));
    }

    @Transactional
    public ItemSupplier create(Long catalogItemId, ItemSupplier value) {
        CatalogItem item = catalogItems.findById(catalogItemId);
        if (value.getSupplier() == null || value.getSupplier().getId() == null) {
            throw new IllegalArgumentException("Выберите поставщика");
        }
        Organization supplier = organizations.findById(value.getSupplier().getId())
                .orElseThrow(() -> new IllegalArgumentException("Организация не найдена"));
        if (!supplier.getRoles().contains(OrganizationRole.SUPPLIER)) {
            throw new IllegalArgumentException("Организация не имеет роли поставщика");
        }
        if (repository.existsByCatalogItemIdAndSupplierId(catalogItemId, supplier.getId())) {
            throw new IllegalArgumentException("Этот поставщик уже добавлен");
        }
        value.setId(null);
        value.setCatalogItem(item);
        value.setSupplier(supplier);
        value.setSupplierArticle(trim(value.getSupplierArticle()));
        value.setNotes(trim(value.getNotes()));
        return repository.save(value);
    }

    @Transactional
    public void delete(Long catalogItemId, Long id) {
        ItemSupplier relation = repository.findById(id).filter(value -> value.getCatalogItem().getId().equals(catalogItemId))
                .orElseThrow(() -> new IllegalArgumentException("Поставщик изделия не найден"));
        repository.delete(relation);
    }

    @Transactional
    public ItemSupplier update(Long catalogItemId, Long id, ItemSupplier values) {
        ItemSupplier relation = find(catalogItemId, id);
        relation.setSupplierArticle(trim(values.getSupplierArticle()));
        relation.setNotes(trim(values.getNotes()));
        return repository.save(relation);
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
