package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

	@GetMapping("/student-login") 
	public String student_login(){
		return "student-login";
	}
	
	//Register Method;
	
	 @PostMapping("/student-login")
	 public String registerStudent(
	            @RequestParam String fullName,
	            @RequestParam String college,
	            @RequestParam String email,
	            @RequestParam String password) {

	        return "student-login";
	    }
	 
	@PostMapping("/student-dasboard")
	public String loginStudent(@RequestParam String email,
			@RequestParam String password)
	{
		
		return "student-dashboard";
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
