package ru.yurch.engflow.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurch.engflow.model.MeasurementUnit;
import java.util.List;
public interface MeasurementUnitRepository extends JpaRepository<MeasurementUnit,Long>{List<MeasurementUnit> findAllByOrderByIdAsc();java.util.Optional<MeasurementUnit> findByName(String name);}
