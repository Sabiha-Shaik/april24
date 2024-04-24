package com.thbs.BatchManagement;

import org.springframework.boot.SpringApplication;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDiscoveryClient
public class BatchManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(BatchManagementApplication.class, args);
	}
 
}
  