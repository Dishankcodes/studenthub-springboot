package com.example.demo.Controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.demo.entity.CertificateTemplate;
import com.example.demo.entity.Course;
import com.example.demo.entity.CourseCertificate;
import com.example.demo.entity.Internships;
import com.example.demo.entity.Student;
import com.example.demo.enums.CertificateType;
import com.example.demo.repository.CertificateTemplateRepository;
import com.example.demo.repository.CourseCertificateRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.LessonProgressRepository;
import com.example.demo.repository.StudentRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class StudentCertificateController {

	@Autowired
	private CourseRepository courseRepo;
	@Autowired
	private LessonProgressRepository lessonProgressRepo;
	@Autowired
	private CourseCertificateRepository certificateRepo;
	@Autowired
	private CertificateTemplateRepository templateRepo;
	@Autowired
	private StudentRepository studentRepo;

	@GetMapping("/student/certificate/{courseId}")
	public String generateCertificate(@PathVariable Integer courseId, HttpSession session, Model model)
			throws Exception {

		Integer studentId = (Integer) session.getAttribute("studentId");
		if (studentId == null) {
			return "redirect:/student-login";
		}

		Student student = studentRepo.findById(studentId).orElseThrow();
		Course course = courseRepo.findById(courseId).orElseThrow();

		/* 1️⃣ Completion check */
		long completed = lessonProgressRepo.countByStudentStudidAndLessonModuleCourseCourseIdAndCompletedTrue(studentId,
				courseId);

		long total = course.getModules().stream().mapToLong(m -> m.getLessons().size()).sum();

		if (completed < total) {
			return "redirect:/student-course-player/" + courseId;
		}

		/* 2️⃣ Already generated? → DOWNLOAD */
		CourseCertificate existing = certificateRepo.findByStudentStudidAndCourseCourseId(studentId, courseId)
				.orElse(null);

		if (existing != null) {
			return "redirect:/student/certificate/download/" + existing.getId();
		}

		/* 3️⃣ Active template */
		CertificateTemplate template = templateRepo.findAll().stream()
				.filter(t -> t.isActive() && t.getType() == CertificateType.COURSE).findFirst().orElse(null);

		if (template == null) {
			session.setAttribute("certificateError",
					"Certificate is not available right now. Please contact admin or try again later.");
			String certMsg = (String) session.getAttribute("certificateError");
			if (certMsg != null) {
				model.addAttribute("certificateError", certMsg);
				session.removeAttribute("certificateError");
			}
			return "redirect:/student-course-player/" + courseId + "?certError=true";
		}

		String certNumber = "CERT-" + System.currentTimeMillis();

		String pdfPath = generatePdf(student, course, template, certNumber);

		/* 5️⃣ Save record */
		CourseCertificate cert = new CourseCertificate();
		cert.setStudent(student);
		cert.setCourse(course);
		cert.setTemplate(template);
		cert.setIssuedAt(LocalDate.now());
		cert.setCertificateNumber("CERT-" + System.currentTimeMillis());
		cert.setPdfPath(pdfPath);
		cert.setCertificateNumber(certNumber);
		certificateRepo.save(cert);

		return "redirect:/student/certificate/download/" + cert.getId();
	}

	@GetMapping("/student/certificate/download/{id}")
	public void downloadCertificate(@PathVariable Integer id, HttpSession session, HttpServletResponse response)
			throws Exception {

		Integer studentId = (Integer) session.getAttribute("studentId");
		if (studentId == null)
			return;

		CourseCertificate cert = certificateRepo.findById(id).orElseThrow();

		if (!cert.getStudent().getStudid().equals(studentId)) {
			return;
		}
		File file = new File(System.getProperty("user.dir") + cert.getPdfPath());

		String safeCourseName = cert.getCourse().getTitle().replaceAll("[^a-zA-Z0-9 ]", "").trim().replace(" ", "_");

		String fileName = "EduPlatform-" + safeCourseName + "-Certificate.pdf";

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

		Files.copy(file.toPath(), response.getOutputStream());
		response.getOutputStream().flush();
	}

	private String generatePdf(Student student, Course course, CertificateTemplate template, String certNumber)
			throws Exception {

		String baseDir = System.getProperty("user.dir") + "/uploads/certificates/student-" + student.getStudid()
				+ "/course-" + course.getCourseId();

		File dir = new File(baseDir);
		if (!dir.exists())
			dir.mkdirs();

		String pdfPath = baseDir + "/certificate.pdf";

		com.lowagie.text.Rectangle pageSize = PageSize.A4.rotate();

		Document document = new Document(pageSize);
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfPath));

		document.open();

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

		BaseFont bold = BaseFont.createFont(BaseFont.TIMES_BOLD, BaseFont.WINANSI, BaseFont.EMBEDDED);
		BaseFont normal = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, BaseFont.EMBEDDED);

		float centerX = pageSize.getWidth() / 2;
		float centerY = pageSize.getHeight() / 2;

		text.beginText();
		text.setFontAndSize(bold, 46);
		text.showTextAligned(Element.ALIGN_CENTER, student.getFullname().toUpperCase(), centerX, centerY + 60, 0);
		text.endText();

		text.beginText();
		text.setFontAndSize(normal, 18);
		text.showTextAligned(Element.ALIGN_CENTER, "has successfully completed the course", centerX, centerY + 10, 0);
		text.endText();

		text.beginText();
		text.setFontAndSize(bold, 24);
		text.showTextAligned(Element.ALIGN_CENTER, course.getTitle(), centerX, centerY - 25, 0);
		text.endText();

		float rightX = pageSize.getWidth() - 60;
		float baseY = 100;

		if (template.getSignatureImage() != null) {
			String signPath = System.getProperty("user.dir") + template.getSignatureImage();
			File signFile = new File(signPath);

			if (signFile.exists()) {
				Image sign = Image.getInstance(signPath);
				sign.scaleToFit(140, 70);
				sign.setAbsolutePosition(rightX - 140, baseY + 50);
				document.add(sign);
			}
		}

		text.beginText();
		text.setFontAndSize(normal, 12);
		text.showTextAligned(Element.ALIGN_RIGHT, "Issued on: " + LocalDate.now(), rightX, baseY, 0);
		text.endText();

		text.beginText();
		text.setFontAndSize(normal, 12);
		text.showTextAligned(Element.ALIGN_RIGHT, "Certificate No: " + certNumber, rightX, baseY - 18, 0);
		text.endText();

		text.beginText();
		text.setFontAndSize(normal, 10);
		text.showTextAligned(Element.ALIGN_RIGHT, "verify/" + certNumber, rightX, baseY - 35, 0);
		text.endText();

		document.close();

		return "/uploads/certificates/student-" + student.getStudid() + "/course-" + course.getCourseId()
				+ "/certificate.pdf";
	}
}