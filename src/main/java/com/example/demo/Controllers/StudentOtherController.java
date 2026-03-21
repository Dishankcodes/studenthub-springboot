package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.InternshipApplication;
import com.example.demo.repository.ApplicationRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class StudentOtherController {

	
	@Autowired
	private ApplicationRepository applicationRepo;
	
	@GetMapping("/student/next-step")
	public String nextStep(@RequestParam Integer internshipId,
	                       HttpSession session,
	                       Model model) {

	    Integer studentId = (Integer) session.getAttribute("studentId");

	    InternshipApplication app =
	        applicationRepo.findByStudent_StudidAndInternship_Id(studentId, internshipId)
	        .orElse(null);

	    model.addAttribute("app", app);

	    return "student-next-step";
	}
}
