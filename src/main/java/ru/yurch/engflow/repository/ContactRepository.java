package ru.yurch.engflow.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurch.engflow.model.Contact;
import java.util.List;
import java.util.Optional;
public interface ContactRepository extends JpaRepository<Contact,Long>{
    List<Contact> findByOrganizationIdOrderByPrimaryDescFullNameAsc(Long organizationId);
    Optional<Contact> findByIdAndOrganizationId(Long id,Long organizationId);
}
