package com.grabpt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GrabptApplication {

	public static void main(String[] args) {
		try {
			SpringApplication.run(GrabptApplication.class, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
