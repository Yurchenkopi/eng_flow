package ru.yurch.engflow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.model.Organization;
import ru.yurch.engflow.repository.OrganizationRepository;
import ru.yurch.engflow.model.OrganizationRole;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public List<Organization> findAll() {
        return organizationRepository.findAll();
    }
    public List<Organization> findCustomers(){return organizationRepository.findDistinctByRolesContainingOrderByNameAsc(OrganizationRole.CUSTOMER);}

    public Organization findById(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Организация не найдена: " + id));
    }

    @Transactional
    public Organization create(Organization organization) {
        organization.setId(null);
        return organizationRepository.save(organization);
    }

    @Transactional
    public Organization update(Long id, Organization values) {
        Organization organization = findById(id);
        organization.setName(values.getName());
        organization.setShortName(values.getShortName());
        organization.setInn(values.getInn());
        organization.setNotes(values.getNotes());
        organization.setRoles(values.getRoles());
        return organizationRepository.save(organization);
    }
}
