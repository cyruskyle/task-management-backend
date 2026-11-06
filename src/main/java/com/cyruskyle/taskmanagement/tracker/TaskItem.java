package com.cyruskyle.taskmanagement.tracker;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks")
public class TaskItem {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 32)
	private String reference;

	@Column(nullable = false, length = 300)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@Column(name = "story_points")
	private Integer storyPoints;

	@Column(name = "assigned_date", nullable = false)
	private LocalDate assignedDate;

	@Column(name = "done_date")
	private LocalDate doneDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private TaskStatus status;

	@Column(nullable = false, length = 2000)
	private String notes;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(
		name = "task_assignees",
		joinColumns = @JoinColumn(name = "task_id")
	)
	@OrderColumn(name = "sort_order")
	@Column(name = "assignee", nullable = false, length = 120)
	private List<String> assignees = new ArrayList<>();

	protected TaskItem() {
	}

	public TaskItem(
			UUID id,
			String reference,
			String name,
			Project project,
			List<String> assignees,
			Integer storyPoints,
			LocalDate assignedDate,
			LocalDate doneDate,
			TaskStatus status,
			String notes) {
		this.id = id;
		update(reference, name, project, assignees, storyPoints, assignedDate, doneDate, status, notes);
	}

	public void update(
			String reference,
			String name,
			Project project,
			List<String> assignees,
			Integer storyPoints,
			LocalDate assignedDate,
			LocalDate doneDate,
			TaskStatus status,
			String notes) {
		this.reference = reference;
		this.name = name;
		this.project = project;
		this.assignees.clear();
		this.assignees.addAll(assignees);
		this.storyPoints = storyPoints;
		this.assignedDate = assignedDate;
		this.doneDate = doneDate;
		this.status = status;
		this.notes = notes;
	}

	public UUID getId() {
		return id;
	}

	public String getReference() {
		return reference;
	}

	public String getName() {
		return name;
	}

	public Project getProject() {
		return project;
	}

	public Integer getStoryPoints() {
		return storyPoints;
	}

	public LocalDate getAssignedDate() {
		return assignedDate;
	}

	public LocalDate getDoneDate() {
		return doneDate;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public String getNotes() {
		return notes;
	}

	public List<String> getAssignees() {
		return assignees;
	}

}
