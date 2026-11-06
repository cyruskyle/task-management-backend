package com.cyruskyle.taskmanagement.tracker;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class TrackerService {

	private final ProjectRepository projectRepository;

	private final TaskRepository taskRepository;

	TrackerService(ProjectRepository projectRepository, TaskRepository taskRepository) {
		this.projectRepository = projectRepository;
		this.taskRepository = taskRepository;
	}

	@Transactional(readOnly = true)
	List<ProjectResponse> listProjects() {
		return projectRepository.findAllByOrderBySortOrderAsc()
			.stream()
			.map(project -> new ProjectResponse(project.getId(), project.getName()))
			.toList();
	}

	@Transactional(readOnly = true)
	List<TaskResponse> listTasks() {
		return taskRepository.findAllForTracker()
			.stream()
			.map(TrackerService::toResponse)
			.toList();
	}

	@Transactional
	TaskResponse createTask(TaskRequest request) {
		Project project = resolveProject(request.project());
		TaskStatus status = resolveStatus(request.status());
		List<String> assignees = normalizeAssignees(request.assignees());
		validateDates(request.assignedDate(), request.doneDate());
		String reference = resolveReference(request.reference(), null, null);

		TaskItem task = new TaskItem(
			UUID.randomUUID(),
			reference,
			request.name().trim(),
			project,
			assignees,
			request.storyPoints(),
			request.assignedDate(),
			request.doneDate(),
			status,
			normalizeNotes(request.notes())
		);

		return toResponse(taskRepository.save(task));
	}

	@Transactional
	TaskResponse updateTask(UUID id, TaskRequest request) {
		TaskItem task = taskRepository.findWithDetailsById(id)
			.orElseThrow(TaskNotFoundException::new);
		Project project = resolveProject(request.project());
		TaskStatus status = resolveStatus(request.status());
		List<String> assignees = normalizeAssignees(request.assignees());
		validateDates(request.assignedDate(), request.doneDate());
		String reference = resolveReference(request.reference(), task.getReference(), id);

		task.update(
			reference,
			request.name().trim(),
			project,
			assignees,
			request.storyPoints(),
			request.assignedDate(),
			request.doneDate(),
			status,
			normalizeNotes(request.notes())
		);

		return toResponse(task);
	}

	private Project resolveProject(String projectName) {
		return projectRepository.findByName(projectName.trim())
			.orElseThrow(() -> new TaskValidationException("Choose an existing project"));
	}

	private static TaskStatus resolveStatus(String status) {
		try {
			return TaskStatus.fromLabel(status.trim());
		}
		catch (IllegalArgumentException exception) {
			throw new TaskValidationException("Choose a valid status");
		}
	}

	private static List<String> normalizeAssignees(List<String> assignees) {
		if (assignees == null) {
			return List.of();
		}

		return assignees.stream()
			.map(String::trim)
			.filter(assignee -> !assignee.isEmpty())
			.distinct()
			.toList();
	}

	private static String normalizeNotes(String notes) {
		return notes == null ? "" : notes.trim();
	}

	private static void validateDates(LocalDate assignedDate, LocalDate doneDate) {
		if (doneDate != null && doneDate.isBefore(assignedDate)) {
			throw new TaskValidationException("Completion date cannot be before the assigned date");
		}
	}

	private String resolveReference(String reference, String currentReference, UUID taskId) {
		String candidate = reference == null ? "" : reference.trim();

		if (candidate.isEmpty()) {
			return currentReference != null ? currentReference : generateReference();
		}

		boolean taken = taskId == null
			? taskRepository.existsByReference(candidate)
			: taskRepository.existsByReferenceAndIdNot(candidate, taskId);

		if (taken) {
			throw new DuplicateReferenceException();
		}

		return candidate;
	}

	private String generateReference() {
		for (int attempt = 0; attempt < 5; attempt++) {
			String candidate = "TASK-" + UUID.randomUUID()
				.toString()
				.replace("-", "")
				.substring(0, 6)
				.toUpperCase(Locale.ROOT);

			if (!taskRepository.existsByReference(candidate)) {
				return candidate;
			}
		}

		throw new IllegalStateException("Could not allocate a task reference");
	}

	private static TaskResponse toResponse(TaskItem task) {
		return new TaskResponse(
			task.getId().toString(),
			task.getReference(),
			task.getName(),
			task.getProject().getName(),
			List.copyOf(task.getAssignees()),
			task.getStoryPoints(),
			task.getAssignedDate(),
			task.getDoneDate(),
			task.getStatus().label(),
			task.getNotes()
		);
	}

}
