package com.payflow_engine;

import org.springframework.boot.SpringApplication;

public class TestPayflowEngineApplication {

	public static void main(String[] args) {
		SpringApplication.from(PayflowEngineApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
