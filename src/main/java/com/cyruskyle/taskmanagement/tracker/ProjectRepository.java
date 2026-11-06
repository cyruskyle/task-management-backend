package com.cyruskyle.taskmanagement.tracker;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProjectRepository extends JpaRepository<Project, Long> {

	List<Project> findAllByOrderBySortOrderAsc();

	Optional<Project> findByName(String name);

}
