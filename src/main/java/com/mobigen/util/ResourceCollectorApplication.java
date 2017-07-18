package com.mobigen.util;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.mobigen.util.service.ResourceCollectService;

@SpringBootApplication
@EnableScheduling
//public class ResourceCollectorApplication implements CommandLineRunner {
public class ResourceCollectorApplication implements CommandLineRunner  {

	@Autowired
	private ResourceCollectService service;

	public static void main(String[] args) {
//		SpringApplication app = new SpringApplication(ResourceCollectorApplication.class);
//		app.run(args);
		SpringApplication.run(ResourceCollectorApplication.class);
	}

//	@Override
//	public void run(String... args) throws Exception {
////		service.getResource();
////		System.exit(0);
//	}
	
	@Scheduled(fixedRate = 1000 * 60)
	public void scheduleTask() throws Exception {
		service.getResource();
	}

	@Override
	public void run(String... arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
