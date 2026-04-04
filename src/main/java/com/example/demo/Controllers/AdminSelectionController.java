package com.example.demo.Controllers;

import java.io.File;
import java.io.FileOutputStream;
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
import com.example.demo.entity.Student;
import com.example.demo.enums.ApplicationStatus;
import com.example.demo.enums.CertificateType;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.CertificateTemplateRepository;
import com.example.demo.repository.InternshipCertificateRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

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


	private String generateInternshipPdf(Student student, Internships internship, CertificateTemplate template) throws Exception {

	    String baseDir = System.getProperty("user.dir") + "/uploads/certificates/student-" 
	            + student.getStudid() + "/internship-" + internship.getId();

	    File dir = new File(baseDir);
	    if (!dir.exists()) dir.mkdirs();

	    String pdfPath = baseDir + "/internship-certificate.pdf";

	    // ✅ Landscape page
	    com.lowagie.text.Rectangle pageSize = PageSize.A4.rotate();

	    Document document = new Document(pageSize);
	    PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfPath));

	    document.open();

	    /* ================= BACKGROUND ================= */
	    if (template.getBackgroundImage() != null) {
	        String bgPath = System.getProperty("user.dir") + template.getBackgroundImage();
	        File bgFile = new File(bgPath);

	        if (bgFile.exists()) {
	            Image bg = Image.getInstance(bgPath);
	            bg.scaleAbsolute(pageSize.getWidth(), pageSize.getHeight());
	            bg.setAbsolutePosition(0, 0);
	            writer.getDirectContentUnder().addImage(bg);
	        }
	    }

	    PdfContentByte text = writer.getDirectContent();

	    BaseFont bold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.EMBEDDED);
	    BaseFont normal = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);

	    // ✅ Center positions
	    float centerX = pageSize.getWidth() / 2;
	    float centerY = pageSize.getHeight() / 2;

	    /* ================= STUDENT NAME ================= */
	    text.beginText();
	    text.setFontAndSize(bold, 34);
	    text.showTextAligned(Element.ALIGN_CENTER,
	            student.getFullname().toUpperCase(),
	            centerX, centerY + 20, 0);
	    text.endText();

	    /* ================= DESCRIPTION ================= */
	    text.beginText();
	    text.setFontAndSize(normal, 16);
	    text.showTextAligned(Element.ALIGN_CENTER,
	            "has successfully completed the Internship Program as " + internship.getRole(),
	            centerX, centerY - 40, 0);
	    text.endText();

	    /* ================= DATE ================= */
	    text.beginText();
	    text.setFontAndSize(normal, 12);
	    text.showTextAligned(Element.ALIGN_LEFT,
	            "Issued on: " + LocalDate.now(),
	            pageSize.getWidth() - 150, 100, 0);
	    text.endText();

	    /* ================= SIGNATURE ================= */
	    if (template.getSignatureImage() != null) {
	        String signPath = System.getProperty("user.dir") + template.getSignatureImage();
	        File signFile = new File(signPath);

	        if (signFile.exists()) {
	            Image sign = Image.getInstance(signPath);
	            sign.scaleToFit(150, 80);

	            // Bottom-right alignment
	            sign.setAbsolutePosition(pageSize.getWidth() - 200, 120);
	            document.add(sign);
	        }
	    }

	    document.close();

	    return "/uploads/certificates/student-" + student.getStudid()
	            + "/internship-" + internship.getId()
	            + "/internship-certificate.pdf";
	}
	
	@PostMapping("/admin/give-certificate")
	public String giveCertificate(@RequestParam Integer appId,
	                             @RequestParam Integer templateId,
	                             RedirectAttributes ra) throws Exception{

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
	    String pdfPath = generateInternshipPdf(
	            app.getStudent(),
	            app.getInternship(),
	            template
	    );

	    cert.setPdfPath(pdfPath);

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
