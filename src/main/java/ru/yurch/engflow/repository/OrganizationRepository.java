package ru.yurch.engflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurch.engflow.model.Organization;
import ru.yurch.engflow.model.OrganizationRole;
import java.util.List;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    List<Organization> findDistinctByRolesContainingOrderByNameAsc(OrganizationRole role);
}
