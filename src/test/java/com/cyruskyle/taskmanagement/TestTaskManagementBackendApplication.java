package com.cyruskyle.taskmanagement;

import org.springframework.boot.SpringApplication;

public class TestTaskManagementBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(TaskManagementBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
