package com.mobigen.util;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.mobigen.util.service.ResourceCollectService;
import com.mobigen.util.service.SystemInfoService;

@SpringBootApplication
@EnableScheduling
public class ResourceCollectorApplication{

	@Autowired
	private ResourceCollectService resourceService;
	
	@Autowired
	private SystemInfoService systemService;

	public static void main(String[] args) {
		SpringApplication.run(ResourceCollectorApplication.class);
	}

	// 1 minute schedule
	@Scheduled(fixedRate = 1000 * 60)
	public void resourceScheduleTask() throws Exception {
		resourceService.getResource();
	}
	
	// 1 hour schedule
	@Scheduled(fixedRate = 1000 * 60 * 60)
	public void systemScheduleTask() throws Exception {
		systemService.getSystem();
	}
	
	
	
	
}
