package com.example.demo.Controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.Service.EmailService;
import com.example.demo.entity.CertificateTemplate;
import com.example.demo.entity.InternshipApplication;
import com.example.demo.entity.InternshipCertificate;
import com.example.demo.entity.Internships;
import com.example.demo.enums.ApplicationStatus;
import com.example.demo.enums.CertificateType;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.CertificateTemplateRepository;
import com.example.demo.repository.InternshipCertificateRepository;

@Controller
public class AdminSelectionController {

	@Autowired
	private ApplicationRepository applicationRepo;

	@Autowired
	private InternshipCertificateRepository internshipCertRepo;

	@Autowired
	private CertificateTemplateRepository templateRepo;

	@Autowired
	private EmailService emailService;

	@GetMapping("/admin-final-selection")
	public String finalSelection(@RequestParam Integer internshipId, Model model) {

		List<InternshipApplication> applications = applicationRepo.findByInternship_IdAndStatusIn(internshipId,
				List.of(ApplicationStatus.PASSED, ApplicationStatus.SELECTED, ApplicationStatus.COMPLETED));

		model.addAttribute("applications", applications);
		model.addAttribute("templates", templateRepo.findByType(CertificateType.INTERNSHIP));
		model.addAttribute("internshipId", internshipId);

		return "admin-final-selection";
	}

	@PostMapping("/admin/select-student")
	public String selectStudent(@RequestParam Integer studentId, @RequestParam Integer internshipId) {

		InternshipApplication app = applicationRepo.findByStudent_StudidAndInternship_Id(studentId, internshipId)
				.orElse(null);

		if (app != null) {
			app.setStatus(ApplicationStatus.SELECTED);

			Internships i = app.getInternship();

			emailService.sendOfferLetter(
			        app.getEmail(),
			        app.getFullName(),
			        i.getTitle(),
			        i.getRole(),
			        i.getType(),
			        i.getLocation(),
			        i.getStipend(),
			        i.getDuration(),
			        i.getStartDate()
			);
			applicationRepo.save(app);
		}

		return "redirect:/admin-final-selection?internshipId=" + internshipId;
	}

	@PostMapping("/admin/give-badge")
	public String giveBadge(@RequestParam Integer appId,
	                        @RequestParam String badgeTitle,
	                        RedirectAttributes ra) {

	    InternshipApplication app = applicationRepo.findById(appId).orElse(null);

	    if (app != null) {
	        app.setBadgeGiven(true);
	        app.setBadgeTitle(badgeTitle);

	        applicationRepo.save(app);

	        // ✅ MESSAGE
	        ra.addFlashAttribute("msg",
	                "🏆 Badge '" + badgeTitle + "' awarded to " + app.getFullName());
	    }

	    return "redirect:/admin-final-selection?internshipId=" + app.getInternship().getId();
	}

	@PostMapping("/admin/give-certificate")
	public String giveCertificate(@RequestParam Integer appId,
	                             @RequestParam Integer templateId,
	                             RedirectAttributes ra) {

	    InternshipApplication app = applicationRepo.findById(appId).orElse(null);

	    if (app == null) {
	        ra.addFlashAttribute("error", "Application not found");
	        return "redirect:/admin-final-selection";
	    }

	    CertificateTemplate template = templateRepo.findById(templateId).orElse(null);

	    if (template == null) {
	        ra.addFlashAttribute("error", "Template not found");
	        return "redirect:/admin-final-selection";
	    }

	    // ✅ CREATE CERTIFICATE
	    InternshipCertificate cert = new InternshipCertificate();
	    cert.setStudent(app.getStudent());
	    cert.setInternship(app.getInternship());
	    cert.setTemplate(template);
	    cert.setIssuedAt(LocalDate.now());
	    cert.setCertificateNumber("INT-" + System.currentTimeMillis());
	    cert.setPdfPath("/certificates/internship/sample.pdf");

	    internshipCertRepo.save(cert);

	    // ✅ UPDATE APPLICATION
	    app.setCertificateGenerated(true);
	    app.setCertificateTemplateId(templateId);
	    app.setStatus(ApplicationStatus.COMPLETED); // 🔥 IMPORTANT

	    applicationRepo.save(app);

	    // ✅ MESSAGE
	    ra.addFlashAttribute("msg",
	            "🎓 Internship completed & certificate generated for " + app.getFullName());

	    return "redirect:/admin-final-selection?internshipId=" + app.getInternship().getId();
	}
}
