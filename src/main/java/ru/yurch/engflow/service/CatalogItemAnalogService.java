package ru.yurch.engflow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yurch.engflow.model.CatalogItem;
import ru.yurch.engflow.model.CatalogItemAnalog;
import ru.yurch.engflow.repository.CatalogItemAnalogRepository;
import java.util.List;

@Service
@Transactional(readOnly=true)
public class CatalogItemAnalogService {
    private final CatalogItemAnalogRepository repository;
    private final CatalogItemService catalogItems;
    public CatalogItemAnalogService(CatalogItemAnalogRepository repository,CatalogItemService catalogItems){this.repository=repository;this.catalogItems=catalogItems;}
    public List<CatalogItemAnalog> relations(Long itemId){catalogItems.findById(itemId);return repository.findAllFor(itemId);}
    public List<CatalogItem> analogs(Long itemId){return relations(itemId).stream().map(relation->relation.other(itemId)).toList();}
    public boolean areAnalogs(Long firstId,Long secondId){if(firstId==null||secondId==null||firstId.equals(secondId))return false;long low=Math.min(firstId,secondId),high=Math.max(firstId,secondId);return repository.existsByFirstCatalogItemIdAndSecondCatalogItemId(low,high);}
    @Transactional public CatalogItemAnalog add(Long itemId,Long analogId){if(itemId==null||analogId==null)throw new IllegalArgumentException("Выберите аналог");if(itemId.equals(analogId))throw new IllegalArgumentException("Изделие не может быть аналогом самому себе");long low=Math.min(itemId,analogId),high=Math.max(itemId,analogId);if(repository.existsByFirstCatalogItemIdAndSecondCatalogItemId(low,high))throw new IllegalArgumentException("Связь аналогов уже существует");CatalogItemAnalog relation=new CatalogItemAnalog();relation.setFirstCatalogItem(catalogItems.findById(low));relation.setSecondCatalogItem(catalogItems.findById(high));return repository.save(relation);}
    @Transactional public void remove(Long itemId,Long relationId){CatalogItemAnalog relation=repository.findById(relationId).orElseThrow(()->new IllegalArgumentException("Связь аналогов не найдена"));if(!relation.getFirstCatalogItem().getId().equals(itemId)&&!relation.getSecondCatalogItem().getId().equals(itemId))throw new IllegalArgumentException("Связь не принадлежит изделию");repository.delete(relation);}
}
