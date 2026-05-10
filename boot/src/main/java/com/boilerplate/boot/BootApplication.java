package com.boilerplate.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication(scanBasePackages = {"com.boilerplate.boot", "com.boilerplate.web"})
@EnableFeignClients(basePackages = "com.boilerplate.infrastructure")
@EnableJpaRepositories(basePackages = "com.boilerplate.infrastructure")
@EntityScan(basePackages = "com.boilerplate.infrastructure")
public class BootApplication {

	public static void main(String[] args) {
		SpringApplication.run(BootApplication.class, args);
	}

}
