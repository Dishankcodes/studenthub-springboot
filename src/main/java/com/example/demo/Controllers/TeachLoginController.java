package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Teacher;
import com.example.demo.repository.TeacherRepo;

@Controller
public class TeachLoginController {

	@Autowired
	private TeacherRepo teacherRepo;

	@GetMapping("/teacher-register")
	public String teacher_register(Model model) {
		model.addAttribute("teacher", new Teacher());
		return "teacher-register";
	}

	@GetMapping("/teacher-auth")
	public String teacher_login() {

		return "teacher-auth";
	}

	@PostMapping("/teacher-auth")
	public String registerTeacher(@ModelAttribute Teacher teacher, Model model) {

		if (teacherRepo.existsByEmail(teacher.getEmail())) {
			model.addAttribute("error", "Email Already Registered !");
			return "teacher-register";
		}

		if (teacherRepo.existsByPhoneno(teacher.getPhoneno())) {
			model.addAttribute("error", "Phone number already registered!");
			return "teacher-register";
		}

		teacherRepo.save(teacher);
		return "redirect:/teacher-auth";

	}

	@PostMapping("/teacher-dashboard")
	public String loginTeacher(@RequestParam String email, Model model) {

		Teacher teacher = teacherRepo.findByemail(email);

		if (teacher == null) {
			model.addAttribute("error", "Email not registered,create your account");
			return "teacher-auth";
		}

		model.addAttribute("teacherName", teacher.getFirstname());
		return "redirect:/teacher-dashboard";

	}

}
