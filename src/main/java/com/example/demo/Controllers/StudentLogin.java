package com.example.demo.Controllers;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Student;
import com.example.demo.repository.StudentRepo;

import jakarta.validation.Valid;

@Controller
public class StudentLogin {

	private final StudentRepo repo;

	public StudentLogin(StudentRepo repo) {
		this.repo = repo;
	}

	// ================== LOGIN + SIGNUP PAGE ==================
	@GetMapping("/student-login")
	public String student_login(Model model, @RequestParam(required = false) String success) {

		model.addAttribute("student", new Student());

		if (success != null) {
			model.addAttribute("success", "Registration successful");
		}
		return "student-login";
	}

	// ================== SIGNUP ==================
	@PostMapping("/student-login")
	public String registerStudent(@Valid @ModelAttribute("stundent") Student student,BindingResult result, 
			Model model) {

		if (repo.existsByEmail(student.getEmail())) {
			model.addAttribute("signupError", "Email already registered");
			model.addAttribute("student", student);
			return "student-login";
		}
		
		
		if(result.hasErrors())
		{
			return "student-login";
		}

		repo.save(student);
		return "redirect:/student-login";
	}

	// ================== LOGIN ==================
	@PostMapping("/student-dashboard")
	public String loginStudent(@RequestParam String email, @RequestParam String password, Model model) {

		Optional<Student> opt = repo.findByEmail(email);

		if (opt.isEmpty() || !password.equals(opt.get().getPassword())) {
			model.addAttribute("loginError", "Invalid email or password");
			model.addAttribute("student", new Student());
			return "student-login";
		}

		return "redirect:/student-dashboard";
	}
}
