package com.aihuishou.bi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SyncApplication {

	public static ApplicationContext ctx;

	public static void main(String[] args) {
		ctx = SpringApplication.run(SyncApplication.class, args);
	}

}
