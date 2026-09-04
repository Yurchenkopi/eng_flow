package ru.yurch.engflow.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yurch.engflow.model.TransferActItem;
import java.math.BigDecimal;
import java.util.List;
public interface TransferActItemRepository extends JpaRepository<TransferActItem,Long>{
    @Query("select coalesce(sum(item.quantity),0) from TransferActItem item where item.projectItem.id=:projectItemId and item.transferAct.transferred=true") BigDecimal transferred(@Param("projectItemId") Long projectItemId);
    @Query("select item.projectItem.id,coalesce(sum(item.quantity),0) from TransferActItem item where item.projectItem.project.id=:projectId and item.transferAct.transferred=true group by item.projectItem.id") List<Object[]> transferredByProject(@Param("projectId") Long projectId);
}
