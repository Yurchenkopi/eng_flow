package ru.yurch.engflow.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurch.engflow.model.TransferAct;

public interface TransferActRepository extends JpaRepository<TransferAct, Long> {

    @EntityGraph(
            attributePaths = {
                    "project",
                    "items",
                    "items.projectItemAllocation",
                    "items.projectItemAllocation.projectItem",
                    "items.projectItemAllocation.projectItem.catalogItem",
                    "items.projectItemAllocation.projectItem.catalogItem.measurementUnit",
                    "items.projectItemAllocation.projectAssembly",
                    "items.projectItemAllocation.projectSubsection"})
    List<TransferAct> findAllByOrderByYearDescNumberDesc();

    @EntityGraph(
            attributePaths = {
                    "project",
                    "items",
                    "items.projectItemAllocation",
                    "items.projectItemAllocation.projectItem",
                    "items.projectItemAllocation.projectItem.catalogItem",
                    "items.projectItemAllocation.projectItem.catalogItem.measurementUnit",
                    "items.projectItemAllocation.projectAssembly",
                    "items.projectItemAllocation.projectSubsection"})
    List<TransferAct> findByProjectIdOrderByYearDescNumberDesc(Long projectId);

    @Override
    @EntityGraph(
            attributePaths = {
                    "project",
                    "items",
                    "items.projectItemAllocation",
                    "items.projectItemAllocation.projectItem",
                    "items.projectItemAllocation.projectItem.catalogItem",
                    "items.projectItemAllocation.projectItem.catalogItem.measurementUnit",
                    "items.projectItemAllocation.projectAssembly",
                    "items.projectItemAllocation.projectSubsection"})
    Optional<TransferAct> findById(Long id);
}
