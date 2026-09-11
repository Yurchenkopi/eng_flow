package ru.yurch.engflow.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yurch.engflow.model.TransferActNumberSequence;

public interface TransferActNumberSequenceRepository extends JpaRepository<TransferActNumberSequence, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select sequence from TransferActNumberSequence sequence where sequence.year=:year")
    Optional<TransferActNumberSequence> findForUpdate(@Param("year") Integer year);
}
