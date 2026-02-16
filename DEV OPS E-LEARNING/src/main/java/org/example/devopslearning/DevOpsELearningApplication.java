package org.example.devopslearning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling 
public class DevOpsELearningApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevOpsELearningApplication.class, args);
	}

}
