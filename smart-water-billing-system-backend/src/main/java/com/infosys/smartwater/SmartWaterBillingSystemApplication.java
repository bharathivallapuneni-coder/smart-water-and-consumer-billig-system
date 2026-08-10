package com.infosys.smartwater;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartWaterBillingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartWaterBillingSystemApplication.class, args);
	}

}
