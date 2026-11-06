package com.cyruskyle.taskmanagement.tracker;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record TaskRequest(
		@Size(max = 32, message = "Reference must be 32 characters or fewer")
		String reference,

		@NotBlank(message = "Task name is required")
		@Size(max = 300, message = "Task name must be 300 characters or fewer")
		String name,

		@NotBlank(message = "Project is required")
		@Size(max = 120, message = "Project must be 120 characters or fewer")
		String project,

		@Size(max = 20, message = "A task can have at most 20 assignees")
		List<@NotBlank(message = "Assignees cannot be blank") @Size(max = 120, message = "Each assignee must be 120 characters or fewer") String> assignees,

		@Min(value = 0, message = "Story points cannot be negative")
		@Max(value = 100, message = "Story points cannot exceed 100")
		Integer storyPoints,

		@NotNull(message = "Assigned date is required")
		LocalDate assignedDate,

		LocalDate doneDate,

		@NotBlank(message = "Status is required")
		String status,

		@Size(max = 2000, message = "Notes must be 2000 characters or fewer")
		String notes) {
}
