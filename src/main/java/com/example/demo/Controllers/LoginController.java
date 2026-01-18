package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

	@GetMapping("/student-login") 
	public String student_login(){
		return "student-login";
	}
	
	@GetMapping("/teacher-auth") 
	public String teacher_login(){
		return "teacher-auth";
	}
	
	@GetMapping("/admin-login") 
	public String admin_login(){
		return "admin-login";
	}
}
