package com.cyruskyle.taskmanagement.tracker;

import java.time.LocalDate;
import java.util.List;

record ProjectResponse(long id, String name) {
}

record TaskResponse(
	String id,
	String reference,
	String name,
	String project,
	List<String> assignees,
	Integer storyPoints,
	LocalDate assignedDate,
	LocalDate doneDate,
	String status,
	String notes
) {
}
