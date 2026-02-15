package com.bookmind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookMindApplication {

	public static void main(String[] args) {
		java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC")); // or "Asia/Kolkata"
		SpringApplication.run(BookMindApplication.class, args);
	}

}
