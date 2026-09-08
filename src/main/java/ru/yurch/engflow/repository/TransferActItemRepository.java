package ru.yurch.engflow.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yurch.engflow.model.TransferActItem;
import java.math.BigDecimal;
import java.util.List;
public interface TransferActItemRepository extends JpaRepository<TransferActItem,Long>{
    @Query("select (count(item)>0) from TransferActItem item where item.projectItemAllocation.projectItem.id=:projectItemId") boolean existsByProjectItemId(@Param("projectItemId") Long projectItemId);
    @Query("select coalesce(sum(item.quantity),0) from TransferActItem item where item.projectItemAllocation.id=:allocationId and item.transferAct.transferred=true") BigDecimal transferred(@Param("allocationId") Long allocationId);
    @Query("select item.projectItemAllocation.id,coalesce(sum(item.quantity),0) from TransferActItem item where item.projectItemAllocation.projectItem.project.id=:projectId and item.transferAct.transferred=true group by item.projectItemAllocation.id") List<Object[]> transferredByProject(@Param("projectId") Long projectId);
}
