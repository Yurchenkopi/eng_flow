package ru.yurch.engflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurch.engflow.model.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}
