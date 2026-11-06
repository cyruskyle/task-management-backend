package com.cyruskyle.taskmanagement.tracker;

public enum TaskStatus {

	RESOLVED("Resolved"),
	REVIEWING("Reviewing"),
	IN_PROGRESS("In Progress"),
	TEST("Test"),
	BLOCKED("Blocked"),
	SHELVED("Shelved");

	private final String label;

	TaskStatus(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}

	public static TaskStatus fromLabel(String label) {
		for (TaskStatus status : values()) {
			if (status.label.equalsIgnoreCase(label)) {
				return status;
			}
		}

		throw new IllegalArgumentException("Unknown task status");
	}

}
