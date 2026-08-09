package com.desenvolvimento.logica.conviva_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
		scanBasePackages = {
				"com.desenvolvimento.logica.conviva"
		}
)
public class ConvivaServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(ConvivaServiceApplication.class, args);
	}
}
