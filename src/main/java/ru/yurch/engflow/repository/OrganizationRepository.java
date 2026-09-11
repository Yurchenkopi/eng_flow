package ru.yurch.engflow.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurch.engflow.model.Organization;
import ru.yurch.engflow.model.OrganizationRole;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    List<Organization> findDistinctByRolesContainingOrderByNameAsc(OrganizationRole role);
}
