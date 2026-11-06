package com.cyruskyle.taskmanagement;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TrackerApiTests {

	private final HttpClient httpClient = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(5))
		.build();

	@Value("${local.server.port}")
	private int port;

	@Test
	void rejectsAnonymousRequests() throws Exception {
		HttpResponse<String> response = get("/api/tasks", null);

		assertThat(response.statusCode()).isEqualTo(401);
		assertThat(response.headers().firstValue("WWW-Authenticate"))
			.hasValueSatisfying(value -> assertThat(value).contains("Basic"));
	}

	@Test
	void rejectsInvalidCredentials() throws Exception {
		String invalidCredentials = basic("test-user", "wrong-password");

		HttpResponse<String> response = get("/api/tasks", invalidCredentials);

		assertThat(response.statusCode()).isEqualTo(401);
	}

	@Test
	void returnsProjectsAndTasksForAuthenticatedUsers() throws Exception {
		String credentials = basic("test-user", "test-password");

		HttpResponse<String> projects = get("/api/projects", credentials);
		HttpResponse<String> tasks = get("/api/tasks", credentials);

		assertThat(projects.statusCode()).isEqualTo(200);
		assertThat(projects.body()).contains("\"name\":\"Core Experience\"");

		assertThat(tasks.statusCode()).isEqualTo(200);
		assertThat(tasks.body()).contains("\"reference\":\"TASK-142\"");
		assertThat(tasks.body()).contains("\"project\":\"Core Experience\"");
		assertThat(tasks.body()).contains("\"status\":\"Resolved\"");
		assertThat(tasks.body()).contains("\"assignees\":[\"J. Lim\",\"M. Reyes\"]");
	}

	@Test
	void createsTaskWithEveryEditableField() throws Exception {
		String credentials = basic("test-user", "test-password");
		String payload = """
			{
			  "reference": "TASK-9001",
			  "name": "Rotate service credentials",
			  "project": "Access Control",
			  "assignees": ["A. Cruz", "J. Lim"],
			  "storyPoints": 5,
			  "assignedDate": "2026-11-05",
			  "doneDate": null,
			  "status": "In Progress",
			  "notes": "Quarterly credential rotation."
			}
			""";

		HttpResponse<String> response = send("POST", "/api/tasks", payload, credentials);

		assertThat(response.statusCode()).isEqualTo(201);
		assertThat(response.headers().firstValue("Location"))
			.hasValueSatisfying(value -> assertThat(value).startsWith("/api/tasks/"));
		assertThat(response.body()).contains("\"reference\":\"TASK-9001\"");
		assertThat(response.body()).contains("\"name\":\"Rotate service credentials\"");
		assertThat(response.body()).contains("\"project\":\"Access Control\"");
		assertThat(response.body()).contains("\"assignees\":[\"A. Cruz\",\"J. Lim\"]");
		assertThat(response.body()).contains("\"storyPoints\":5");
		assertThat(response.body()).contains("\"status\":\"In Progress\"");
		assertThat(response.body()).contains("\"notes\":\"Quarterly credential rotation.\"");
	}

	@Test
	void generatesReferenceWhenItIsBlank() throws Exception {
		String credentials = basic("test-user", "test-password");
		String payload = """
			{
			  "reference": "",
			  "name": "Generate task reference",
			  "project": "Workflow",
			  "assignees": [],
			  "storyPoints": null,
			  "assignedDate": "2026-11-05",
			  "doneDate": null,
			  "status": "Reviewing",
			  "notes": ""
			}
			""";

		HttpResponse<String> response = send("POST", "/api/tasks", payload, credentials);

		assertThat(response.statusCode()).isEqualTo(201);
		assertThat(response.body()).containsPattern("\"reference\":\"TASK-[0-9A-F]{6}\"");
	}

	@Test
	void updatesEveryEditableField() throws Exception {
		String credentials = basic("test-user", "test-password");
		String payload = """
			{
			  "reference": "TASK-212-EDIT",
			  "name": "Polish empty, loading, and error states",
			  "project": "Workflow",
			  "assignees": ["T. Santos"],
			  "storyPoints": 5,
			  "assignedDate": "2026-11-01",
			  "doneDate": "2026-11-06",
			  "status": "Resolved",
			  "notes": "All states covered."
			}
			""";

		HttpResponse<String> response = send(
			"PUT", "/api/tasks/00000000-0000-0000-0000-000000000212", payload, credentials);

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"reference\":\"TASK-212-EDIT\"");
		assertThat(response.body()).contains("\"name\":\"Polish empty, loading, and error states\"");
		assertThat(response.body()).contains("\"project\":\"Workflow\"");
		assertThat(response.body()).contains("\"assignees\":[\"T. Santos\"]");
		assertThat(response.body()).contains("\"storyPoints\":5");
		assertThat(response.body()).contains("\"assignedDate\":\"2026-11-01\"");
		assertThat(response.body()).contains("\"doneDate\":\"2026-11-06\"");
		assertThat(response.body()).contains("\"status\":\"Resolved\"");
		assertThat(response.body()).contains("\"notes\":\"All states covered.\"");
	}

	@Test
	void rejectsInvalidTaskData() throws Exception {
		String credentials = basic("test-user", "test-password");
		String payload = """
			{
			  "name": "",
			  "project": "Workflow",
			  "assignees": [],
			  "storyPoints": 101,
			  "assignedDate": "2026-11-05",
			  "doneDate": null,
			  "status": "In Progress",
			  "notes": ""
			}
			""";

		HttpResponse<String> response = send("POST", "/api/tasks", payload, credentials);

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).contains("Task name is required");
		assertThat(response.body()).contains("Story points cannot exceed 100");
	}

	@Test
	void rejectsCompletionDateBeforeAssignedDate() throws Exception {
		String credentials = basic("test-user", "test-password");
		String payload = """
			{
			  "name": "Invalid date order",
			  "project": "Workflow",
			  "assignees": [],
			  "storyPoints": 1,
			  "assignedDate": "2026-11-05",
			  "doneDate": "2026-11-01",
			  "status": "Resolved",
			  "notes": ""
			}
			""";

		HttpResponse<String> response = send("POST", "/api/tasks", payload, credentials);

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).contains("Completion date cannot be before the assigned date");
	}

	@Test
	void rejectsDuplicateReferences() throws Exception {
		String credentials = basic("test-user", "test-password");
		String payload = """
			{
			  "reference": "TASK-142",
			  "name": "Duplicate reference",
			  "project": "Workflow",
			  "assignees": [],
			  "storyPoints": 1,
			  "assignedDate": "2026-11-05",
			  "doneDate": null,
			  "status": "In Progress",
			  "notes": ""
			}
			""";

		HttpResponse<String> response = send("POST", "/api/tasks", payload, credentials);

		assertThat(response.statusCode()).isEqualTo(409);
	}

	@Test
	void rejectsAnonymousWrites() throws Exception {
		String payload = """
			{
			  "name": "Anonymous task",
			  "project": "Workflow",
			  "assignedDate": "2026-11-05",
			  "status": "In Progress"
			}
			""";

		HttpResponse<String> response = send("POST", "/api/tasks", payload, null);

		assertThat(response.statusCode()).isEqualTo(401);
	}

	private HttpResponse<String> get(String path, String authorization) throws IOException, InterruptedException {
		return send("GET", path, null, authorization);
	}

	private HttpResponse<String> send(String method, String path, String body, String authorization)
			throws IOException, InterruptedException {
		var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
			.timeout(Duration.ofSeconds(10))
			.method(method, body == null
					? HttpRequest.BodyPublishers.noBody()
					: HttpRequest.BodyPublishers.ofString(body));

		if (body != null) {
			builder.header("Content-Type", "application/json");
		}

		if (authorization != null) {
			builder.header("Authorization", authorization);
		}

		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	private static String basic(String username, String password) {
		var value = username + ":" + password;
		return "Basic " + Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

}
