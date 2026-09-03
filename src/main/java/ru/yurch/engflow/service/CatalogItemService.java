package ru.yurch.engflow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.model.CatalogItem;
import ru.yurch.engflow.repository.CatalogItemRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service @Transactional(readOnly = true)
public class CatalogItemService {
    private final CatalogItemRepository repository;
    public CatalogItemService(CatalogItemRepository repository) { this.repository = repository; }
    public List<CatalogItem> findAll(String query) { return findAll(query, "name", "asc"); }
    public List<CatalogItem> findAll(String query, String sort, String direction) {
        Sort order = Sort.by(Sort.Direction.fromOptionalString(direction).orElse(Sort.Direction.ASC), catalogSortProperty(sort));
        return query == null || query.isBlank() ? repository.findAll(order) : repository.search(query.trim(), order);
    }
    public List<CatalogItem> autocomplete(String query) { return query == null || query.isBlank() ? List.of() : repository.autocomplete(query.trim(), PageRequest.of(0, 15)); }
    public CatalogItem findById(Long id) { return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Изделие не найдено: " + id)); }
    @Transactional public CatalogItem create(CatalogItem item) { item.setId(null); normalize(item); return repository.save(item); }
    public CatalogItem prepareCopy(Long id) {
        CatalogItem source = findById(id); CatalogItem copy = new CatalogItem();
        copy.setDesignation(source.getDesignation()); copy.setName(source.getName()); copy.setManufacturer(source.getManufacturer()); copy.setUnit(source.getUnit()); copy.setNotes(source.getNotes());
        return copy;
    }
    @Transactional public CatalogItem update(Long id, CatalogItem values) {
        CatalogItem item = findById(id); normalize(values);
        item.setDesignation(values.getDesignation()); item.setName(values.getName()); item.setManufacturer(values.getManufacturer()); item.setUnit(values.getUnit()); item.setNotes(values.getNotes());
        return repository.save(item);
    }
    private void normalize(CatalogItem item) {
        item.setDesignation(trimToNull(item.getDesignation())); item.setName(item.getName() == null ? null : item.getName().trim());
        item.setManufacturer(trimToNull(item.getManufacturer())); item.setUnit(item.getUnit() == null ? null : item.getUnit().trim());
    }
    private String trimToNull(String value) { if (value == null || value.trim().isEmpty()) return null; return value.trim(); }
    private String catalogSortProperty(String value) { return switch (value == null ? "" : value) { case "designation" -> "designation"; case "manufacturer" -> "manufacturer"; case "unit" -> "unit"; default -> "name"; }; }
}
