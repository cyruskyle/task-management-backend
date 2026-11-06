package com.cyruskyle.taskmanagement.tracker;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
class TrackerController {

	private final TrackerService trackerService;

	TrackerController(TrackerService trackerService) {
		this.trackerService = trackerService;
	}

	@GetMapping("/projects")
	List<ProjectResponse> listProjects() {
		return trackerService.listProjects();
	}

	@GetMapping("/tasks")
	List<TaskResponse> listTasks() {
		return trackerService.listTasks();
	}

	@PostMapping("/tasks")
	ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
		TaskResponse task = trackerService.createTask(request);
		return ResponseEntity.created(URI.create("/api/tasks/" + task.id())).body(task);
	}

	@PutMapping("/tasks/{id}")
	TaskResponse updateTask(@PathVariable UUID id, @Valid @RequestBody TaskRequest request) {
		return trackerService.updateTask(id, request);
	}

}
