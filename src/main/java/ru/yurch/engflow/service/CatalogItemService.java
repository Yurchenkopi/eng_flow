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
    private final CatalogItemRepository repository; private final MeasurementUnitService units;
    public CatalogItemService(CatalogItemRepository repository,MeasurementUnitService units) { this.repository = repository;this.units=units; }
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
        copy.setDesignation(source.getDesignation()); copy.setName(source.getName()); copy.setManufacturer(source.getManufacturer()); copy.setMeasurementUnit(source.getMeasurementUnit()); copy.setNotes(source.getNotes());
        return copy;
    }
    @Transactional public CatalogItem update(Long id, CatalogItem values) {
        CatalogItem item = findById(id); normalize(values);
        item.setDesignation(values.getDesignation()); item.setName(values.getName()); item.setManufacturer(values.getManufacturer()); item.setMeasurementUnit(values.getMeasurementUnit()); item.setNotes(values.getNotes());
        return repository.save(item);
    }
    private void normalize(CatalogItem item) {
        item.setDesignation(trimToNull(item.getDesignation())); item.setName(item.getName() == null ? null : item.getName().trim());
        item.setManufacturer(trimToNull(item.getManufacturer()));
        if(item.getMeasurementUnit()==null||item.getMeasurementUnit().getId()==null)throw new IllegalArgumentException("Укажите единицу измерения");
        item.setMeasurementUnit(units.findById(item.getMeasurementUnit().getId()));
    }
    private String trimToNull(String value) { if (value == null || value.trim().isEmpty()) return null; return value.trim(); }
    private String catalogSortProperty(String value) { return switch (value == null ? "" : value) { case "designation" -> "designation"; case "manufacturer" -> "manufacturer"; case "unit" -> "measurementUnit.name"; default -> "name"; }; }
}
