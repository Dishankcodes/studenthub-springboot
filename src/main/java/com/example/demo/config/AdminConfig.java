package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.entity.Admin;
import com.example.demo.repository.AdminRepository;

@Configuration
public class AdminConfig {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Bean
	CommandLineRunner init(AdminRepository repo) {
		return args -> {

			if (repo.findByEmail("eduplatform2026@gmail.com").isEmpty()) {

				Admin admin = new Admin();
				admin.setUsername("Dishank");
				admin.setEmail("eduplatform2026@gmail.com");
				admin.setPassword(passwordEncoder.encode("1234"));

				repo.save(admin);

				System.out.println("Default Admin Created!");
			} else {
				System.out.println("Admin already exists");
			}
		};
	}
}