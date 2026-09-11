package ru.yurch.engflow.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.model.MeasurementUnit;
import ru.yurch.engflow.repository.MeasurementUnitRepository;

@Service
@Transactional(readOnly = true)
public class MeasurementUnitService {

    private final MeasurementUnitRepository repository;

    public MeasurementUnitService(MeasurementUnitRepository repository) {
        this.repository = repository;
    }

    public List<MeasurementUnit> findAll() {
        return repository.findAllByOrderByIdAsc();
    }

    public MeasurementUnit findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Единица измерения не найдена: " + id));
    }
}
