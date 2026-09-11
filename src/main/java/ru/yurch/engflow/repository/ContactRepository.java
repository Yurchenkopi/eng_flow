package ru.yurch.engflow.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurch.engflow.model.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findByOrganizationIdOrderByPrimaryDescFullNameAsc(Long organizationId);

    Optional<Contact> findByIdAndOrganizationId(Long id, Long organizationId);
}
