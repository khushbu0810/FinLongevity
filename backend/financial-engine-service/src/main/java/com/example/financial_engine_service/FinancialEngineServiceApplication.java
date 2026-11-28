package com.example.financial_engine_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FinancialEngineServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinancialEngineServiceApplication.class, args);
	}

}
