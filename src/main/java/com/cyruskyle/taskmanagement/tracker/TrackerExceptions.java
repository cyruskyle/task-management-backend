package com.cyruskyle.taskmanagement.tracker;

final class TaskValidationException extends RuntimeException {

	TaskValidationException(String message) {
		super(message);
	}

}

final class TaskNotFoundException extends RuntimeException {

	TaskNotFoundException() {
		super("Task not found");
	}

}

final class DuplicateReferenceException extends RuntimeException {

	DuplicateReferenceException() {
		super("That task reference is already in use");
	}

}
