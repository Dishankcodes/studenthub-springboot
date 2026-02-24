package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.demo.entity.CourseCertificate;
import com.example.demo.repository.CourseCertificateRepository;

@Controller
public class IndexController {

	
	@Autowired
	private CourseCertificateRepository certificateRepo;
	
	@GetMapping("/verify/{certNumber}")
	public String certificateVerify(@PathVariable String certNumber,
			Model model) {
		
		
		CourseCertificate cert= certificateRepo.findByCertificateNumber(certNumber).orElse(null);
				
				
		if (certNumber == null) {
		model.addAttribute("invalid", true);
		return "certificate-verify";
		}
		
		model.addAttribute("cert", cert);
		return "certificate-verify";
	}
	@GetMapping("/index")
	public String index() {
		return "index";
	}

	@GetMapping("/courses")
	public String home_course() {
		return "courses";
	}

	@GetMapping("/about")
	public String student_about() {
		return "about";
	}

	@GetMapping("/blog")
	public String blog() {
		return "blog";
	}

	@GetMapping("/career")
	public String student_career() {
		return "career";
	}

	@GetMapping("/student-privacy")
	public String student_privacy() {
		return "student-privacy";
	}

	@GetMapping("/teacher-homepage")
	public String teacher_homepage() {
		return "teacher-homepage";
	}

	@GetMapping("/teacher_about")
	public String teacher_about() {
		return "teacher-about";
	}

	@GetMapping("/teacher-privacy")
	public String t_privacy() {
		return "teacher-privacy";
	}

	@GetMapping("/admin-homepage")
	public String admin_homepage() {
		return "admin-homepage";
	}

	@GetMapping("/admin_about")
	public String admin_about() {
		return "admin-about";
	}
}
