package com.mobigen.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.mobigen.util.service.ResourceCollectService;

@SpringBootApplication
public class ResourceCollectorApplication implements CommandLineRunner {

	@Autowired
	private ResourceCollectService service;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ResourceCollectorApplication.class);
		app.run(args);
	}

	@Override
	public void run(String... args) throws Exception {
		service.getResource();
		System.exit(0);
	}
}
