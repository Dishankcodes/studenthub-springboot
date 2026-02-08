package com.example.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.Student;
import com.example.demo.entity.Teacher;
import com.example.demo.repository.StudentRepo;
import com.example.demo.repository.TeacherRepo;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

	@Autowired
	private StudentRepo repo;

	@Autowired
	TeacherRepo teacherRepo;

	@GetMapping("/admin-dashboard")
	public String admin_dashboard(HttpSession session, Model model) {
		if (session.getAttribute("adminEmail") == null) {
			return "redirect:/admin-login";
		}

		String username = (String) session.getAttribute("adminUsername");
		model.addAttribute("username", username);
		return "admin-dashboard";
	}

	@GetMapping("/manage-students")
	public String admin_student(HttpSession session, Model model) {

		if (session.getAttribute("adminEmail") == null) {
			return "redirect:/admin-login";
		}
		List<Student> students = repo.findAll();
		model.addAttribute("students", students);

		String username = (String) session.getAttribute("adminUsername");
		model.addAttribute("username", username);

		return "manage-students";
	}

	@GetMapping("/manage-teachers")
	public String admin_instructor(HttpSession session, Model model) {

		if (session.getAttribute("adminEmail") == null) {
			return "redirect:/admin-login";
		}

		List<Teacher> teachers = teacherRepo.findAll();
		model.addAttribute("teachers", teachers);

		String username = (String) session.getAttribute("adminUsername");
		model.addAttribute("username", username);

		return "manage-instructor";
	}

	@GetMapping("/manage-courses")
	public String admin_courses() {

		return "manage-courses";
	}

	@GetMapping("/manage-internships")
	public String admin_internships() {

		return "manage-internships";
	}

	@GetMapping("/admin-feedback")
	public String admin_feedback() {

		return "admin-feedback";
	}

	@GetMapping("/admin-settings")
	public String admin_settings() {

		return "admin-settings";
	}

	@GetMapping("/admin-logout")
	public String adminLogout(HttpSession session) {
		session.invalidate();
		return "redirect:/admin-login";
	}

}
