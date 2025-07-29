package com.grabpt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class GrabptApplication {

	public static void main(String[] args) {
		try {
			SpringApplication.run(GrabptApplication.class, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
