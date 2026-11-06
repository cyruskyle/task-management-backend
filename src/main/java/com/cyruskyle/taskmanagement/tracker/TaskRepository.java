package com.cyruskyle.taskmanagement.tracker;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface TaskRepository extends JpaRepository<TaskItem, UUID> {

	@EntityGraph(attributePaths = { "project", "assignees" })
	@Query("select task from TaskItem task order by task.assignedDate desc, task.reference desc")
	List<TaskItem> findAllForTracker();

	@EntityGraph(attributePaths = { "project", "assignees" })
	@Query("select task from TaskItem task where task.id = :id")
	Optional<TaskItem> findWithDetailsById(@Param("id") UUID id);

	boolean existsByReference(String reference);

	boolean existsByReferenceAndIdNot(String reference, UUID id);

}
