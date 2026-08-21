package com.ayuntamiento.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
	    "com.ayuntamiento.auth",
	    "com.ayuntamiento.security_lib"
	})
@EnableFeignClients(basePackages = {
	    "com.ayuntamiento.auth.client"
	})
public class AuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}

}
