package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Student;
import com.example.demo.repository.StudentRepo;

@Controller
public class LoginController {

	@Autowired
	private StudentRepo repo;
	
	
	@GetMapping("/student-login") 
	public String student_login(Model model){
		model.addAttribute("student", new Student()); 
		return "student-login";
	}
	
	//Register Method;
	
	 @PostMapping("/student-login")
	 public String registerStudent(@ModelAttribute Student stud,Model model) {

		if (repo.existsByEmail(stud.getEmail())) {
	            model.addAttribute("error", "Email already registered");
	            return "student-login";
	        } 
		
		repo.save(stud);
		model.addAttribute("success", "Registration successful");
	    return "redirect:/student-login";
	    }
	 
	@PostMapping("/student-dashboard")
	public String loginStudent(
            Model model)
	{

		return "redirect:/student-dashboard";
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
