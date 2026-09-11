package ru.yurch.engflow.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurch.engflow.model.MeasurementUnit;

public interface MeasurementUnitRepository extends JpaRepository<MeasurementUnit, Long> {

    List<MeasurementUnit> findAllByOrderByIdAsc();

    java.util.Optional<MeasurementUnit> findByName(String name);
}
