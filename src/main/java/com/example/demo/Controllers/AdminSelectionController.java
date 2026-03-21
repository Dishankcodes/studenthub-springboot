package com.example.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.CertificateTemplate;
import com.example.demo.entity.InternshipApplication;
import com.example.demo.enums.ApplicationStatus;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.CertificateTemplateRepository;

@Controller
public class AdminSelectionController {


	@Autowired
	private ApplicationRepository applicationRepo;
	
	@Autowired
	private CertificateTemplateRepository templateRepo;
	
	
	@GetMapping("/admin/final-selection")
	public String finalSelection(@RequestParam Integer internshipId, Model model) {

	    List<InternshipApplication> app =
	        applicationRepo.findByInternship_IdAndStatus(
	            internshipId,
	            ApplicationStatus.PASSED
	        );

	    model.addAttribute("app", app);
	    model.addAttribute("internshipId", internshipId);

	    return "admin-final-selection";
	}
	
	@PostMapping("/admin/select-student")
	public String selectStudent(@RequestParam Integer appId) {

	    InternshipApplication app = applicationRepo.findById(appId).orElse(null);

	    if (app != null) {
	        app.setStatus(ApplicationStatus.SELECTED);
	        applicationRepo.save(app);
	    }

	    return "redirect:/admin-final-selection?internshipId=" + app.getInternship().getId();
	}
	
	@PostMapping("/admin/give-badge")
	public String giveBadge(@RequestParam Integer appId) {

	    InternshipApplication app = applicationRepo.findById(appId).orElse(null);

	    if (app != null) {
	        app.setBadgeGiven(true);
	        applicationRepo.save(app);
	    }

	    return "redirect:/admin-final-selection?internshipId=" + app.getInternship().getId();
	}
	

	@PostMapping("/admin/generate-internship-certificate")
	public String generateCertificate(@RequestParam Integer appId) {

	    InternshipApplication app = applicationRepo.findById(appId).orElse(null);

	    if (app != null) {

	        CertificateTemplate template =
	            templateRepo.findByActiveTrue().orElse(null);

	        if (template != null && template.getType().name().equals("INTERNSHIP")) {

	            // 🔥 Just mark generated (PDF logic you already have)
	            app.setCertificateGenerated(true);
	            app.setStatus(ApplicationStatus.COMPLETED);

	            applicationRepo.save(app);
	        }
	    }

	    return "redirect:/admin/final-selection?internshipId=" + app.getInternship().getId();
	}
}
