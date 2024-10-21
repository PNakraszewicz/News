package com.interview.news;

import org.springframework.boot.SpringApplication;

public class TestNewsApplication {

	public static void main(String[] args) {
		SpringApplication.from(NewsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
