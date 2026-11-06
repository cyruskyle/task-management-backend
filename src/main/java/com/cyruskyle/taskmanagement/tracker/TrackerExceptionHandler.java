package com.cyruskyle.taskmanagement.tracker;

import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
class TrackerExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail handleInvalidBody(MethodArgumentNotValidException exception) {
		String detail = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.sorted()
			.distinct()
			.limit(5)
			.collect(Collectors.joining("; "));

		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problem.setTitle("Invalid task");
		problem.setDetail(detail.isBlank() ? "The task data is invalid" : detail);
		return problem;
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ProblemDetail handleUnreadableBody() {
		return problem(HttpStatus.BAD_REQUEST, "Invalid task", "The request body could not be read");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ProblemDetail handleInvalidIdentifier() {
		return problem(HttpStatus.BAD_REQUEST, "Invalid task", "The task identifier is invalid");
	}

	@ExceptionHandler(TaskValidationException.class)
	ProblemDetail handleTaskValidation(TaskValidationException exception) {
		return problem(HttpStatus.BAD_REQUEST, "Invalid task", exception.getMessage());
	}

	@ExceptionHandler(TaskNotFoundException.class)
	ProblemDetail handleTaskNotFound(TaskNotFoundException exception) {
		return problem(HttpStatus.NOT_FOUND, "Task not found", exception.getMessage());
	}

	@ExceptionHandler(DuplicateReferenceException.class)
	ProblemDetail handleDuplicateReference(DuplicateReferenceException exception) {
		return problem(HttpStatus.CONFLICT, "Task conflict", exception.getMessage());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ProblemDetail handleDataConflict() {
		return problem(HttpStatus.CONFLICT, "Task conflict", "The change conflicts with existing data");
	}

	private static ProblemDetail problem(HttpStatus status, String title, String detail) {
		ProblemDetail problem = ProblemDetail.forStatus(status);
		problem.setTitle(title);
		problem.setDetail(detail);
		return problem;
	}

}
