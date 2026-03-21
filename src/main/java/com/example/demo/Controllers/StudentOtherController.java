package com.example.demo.Controllers;

import java.io.File;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.InternshipApplication;
import com.example.demo.entity.InternshipCertificate;
import com.example.demo.enums.ApplicationStatus;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.InternshipCertificateRepository;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class StudentOtherController {

	
	@Autowired
	private ApplicationRepository applicationRepo;
	
	@Autowired
	private InternshipCertificateRepository internshipCertRepo;
	
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

	@PostMapping("/student/reapply")
	public String reapply(@RequestParam Integer internshipId,
	                      HttpSession session) {

	    Integer studentId = (Integer) session.getAttribute("studentId");

	    InternshipApplication app =
	        applicationRepo.findByStudent_StudidAndInternship_Id(studentId, internshipId)
	        .orElse(null);

	    if (app != null) {
	        app.setStatus(ApplicationStatus.PENDING);
	        app.setAllowReattempt(false);
	        applicationRepo.save(app);
	    }

	    return "redirect:/student-internship-detail?id=" + internshipId;
	}
	
	
	
	
	@GetMapping("/student/internship/certificate/{internshipId}")
	public void downloadInternshipCertificate(@PathVariable Integer internshipId,
	                                          HttpSession session,
	                                          HttpServletResponse response) throws Exception {

	    Integer studentId = (Integer) session.getAttribute("studentId");

	    InternshipCertificate cert =
	        internshipCertRepo
	        .findByStudentStudidAndInternshipId(studentId, internshipId)
	        .orElse(null);

	    if (cert == null) return;

	    File file = new File(System.getProperty("user.dir") + cert.getPdfPath());

	    response.setContentType("application/pdf");
	    response.setHeader("Content-Disposition", "attachment; filename=internship-certificate.pdf");

	    Files.copy(file.toPath(), response.getOutputStream());
	    response.getOutputStream().flush();
	}
}
