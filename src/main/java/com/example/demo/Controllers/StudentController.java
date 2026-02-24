package com.example.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.CourseCertificate;
import com.example.demo.repository.CourseCertificateRepository;

import jakarta.mail.Session;
import jakarta.servlet.http.HttpSession;

@Controller
public class StudentController {
	
	@Autowired
	private CourseCertificateRepository certificateRepo;

	@GetMapping("/student-dashboard")
	public String student_dashboard() {

		return "student-dashboard";
	}

	@GetMapping("/student-learning")
	public String student_learning(HttpSession session, Model model) {

		Integer studentId = (Integer) session.getAttribute("studentId");
		
		List<CourseCertificate> certificates = certificateRepo.findByStudentId(studentId);
		model.addAttribute("certificates", certificates);
		return "student-learning";
	}

	@GetMapping("/student-news")
	public String student_announcements() {
		return "student-announcements";
	}

	@GetMapping("/student-notes")
	public String student_stuff() {
		return "student-stuff";
	}

	@GetMapping("/student-chat")
	public String student_chat() {
		return "student-chat";
	}

	@GetMapping("/student-internships")
	public String student_internships() {
		return "student-internships";
	}

	@GetMapping("/student-notification")
	public String student_notifi() {
		return "student-notification";
	}

	@GetMapping("/student-profile")
	public String student_profile() {
		return "student-profile";
	}

	@GetMapping("/student-instructor-feedback")
	public String student_instructor_feedback() {
		return "student_inst_review";
	}

}
