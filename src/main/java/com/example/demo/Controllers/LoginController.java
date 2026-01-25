package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

		 //String name="fullname";
		 
	    return "student-login";
	    }
	 
	@PostMapping("/student-dashboard")
	public String loginStudent(@RequestParam String email,
			@RequestParam String password,
            Model model)
	{

	    model.addAttribute("name",email);
		return "student-dashboard";
	}
	
	
	@GetMapping("/teacher-auth") 
	public String teacher_login(){
		return "teacher-auth";
	}
	
	@GetMapping("/teacher-register")
	public String teacher_register() {
		return "teacher-register";
	}
	@GetMapping("/admin-login") 
	public String admin_login(){
		return "admin-login";
	}
	
	@PostMapping("/admin-dashboard")
	public String loginAdmin(
			@RequestParam String username,
			@RequestParam String email,
			@RequestParam String password,
            Model model)
	{

	    model.addAttribute("username",username);
		return "admin-dashboard";
	}

}
