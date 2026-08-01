package com.payflow_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration;

@SpringBootApplication
public class PayflowEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(PayflowEngineApplication.class, args);
	}

}
