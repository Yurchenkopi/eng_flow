package ru.yurch.engflow.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.model.Contact;
import ru.yurch.engflow.repository.ContactRepository;
import java.util.List;
@Service @Transactional(readOnly=true)
public class ContactService{
    private final ContactRepository repository; private final OrganizationService organizations;
    public ContactService(ContactRepository repository,OrganizationService organizations){this.repository=repository;this.organizations=organizations;}
    public List<Contact> findByOrganization(Long id){return repository.findByOrganizationIdOrderByPrimaryDescFullNameAsc(id);}
    public Contact find(Long organizationId,Long id){return repository.findByIdAndOrganizationId(id,organizationId).orElseThrow(()->new IllegalArgumentException("Контакт не найден: "+id));}
    @Transactional public Contact create(Long organizationId,Contact contact){contact.setId(null);contact.setOrganization(organizations.findById(organizationId));return repository.save(contact);}
    @Transactional public Contact update(Long organizationId,Long id,Contact values){Contact contact=find(organizationId,id);contact.setFullName(values.getFullName());contact.setPosition(values.getPosition());contact.setEmail(values.getEmail());contact.setPhone(values.getPhone());contact.setPrimary(values.isPrimary());contact.setNotes(values.getNotes());return repository.save(contact);}
}
