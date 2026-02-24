package com.example.demo.Controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.demo.entity.CertificateTemplate;
import com.example.demo.entity.Course;
import com.example.demo.entity.CourseCertificate;
import com.example.demo.entity.Student;
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
	public String generateCertificate(@PathVariable Integer courseId, HttpSession session) throws Exception {

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
		CertificateTemplate template = templateRepo.findByActiveTrue()
				.orElseThrow(() -> new RuntimeException("No active certificate template"));

		/* 4️⃣ Generate PDF */
		String pdfPath = generatePdf(student, course, template);

		/* 5️⃣ Save record */
		CourseCertificate cert = new CourseCertificate();
		cert.setStudent(student);
		cert.setCourse(course);
		cert.setTemplate(template);
		cert.setIssuedAt(LocalDate.now());
		cert.setCertificateNumber("CERT-" + System.currentTimeMillis());
		cert.setPdfPath(pdfPath);

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

		// 🔒 ownership check
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
	/* ================= PDF GENERATOR ================= */

	private String generatePdf(Student student, Course course, CertificateTemplate template) throws Exception {

		String baseDir = System.getProperty("user.dir") + "/uploads/certificates/student-" + student.getStudid()
				+ "/course-" + course.getCourseId();

		File dir = new File(baseDir);
		if (!dir.exists())
			dir.mkdirs();

		String pdfPath = baseDir + "/certificate.pdf";

		Document document = new Document(PageSize.A4);
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfPath));

		document.open();

		/* Background */
		Image bg = Image.getInstance(System.getProperty("user.dir") + template.getBackgroundImage());
		bg.scaleAbsolute(PageSize.A4.getWidth(), PageSize.A4.getHeight());
		bg.setAbsolutePosition(0, 0);
		writer.getDirectContentUnder().addImage(bg);

		/* Text */
		PdfContentByte text = writer.getDirectContent();
		BaseFont bold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.EMBEDDED);

		BaseFont normal = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);

		/* ================= STUDENT NAME ================= */
		text.beginText();
		text.setFontAndSize(bold, 32);
		text.showTextAligned(Element.ALIGN_CENTER, student.getFullname().toUpperCase(), PageSize.A4.getWidth() / 2, 430,
				0);
		text.endText();

		/* ================= COURSE NAME ================= */
		text.beginText();
		text.setFontAndSize(normal, 20);
		text.showTextAligned(Element.ALIGN_CENTER, course.getTitle(), PageSize.A4.getWidth() / 2, 380, 0);
		text.endText();

		/* ================= ISSUED DATE ================= */
		text.beginText();
		text.setFontAndSize(normal, 13);
		text.showTextAligned(Element.ALIGN_CENTER, "Issued on: " + LocalDate.now(), PageSize.A4.getWidth() / 2, 340, 0);
		text.endText();
		/* Signature */
		if (template.getSignatureImage() != null) {
			Image sign = Image.getInstance(System.getProperty("user.dir") + template.getSignatureImage());
			sign.scaleToFit(120, 60);
			sign.setAbsolutePosition(400, 150);
			document.add(sign);
		}

		document.close();

		return "/uploads/certificates/student-" + student.getStudid() + "/course-" + course.getCourseId()
				+ "/certificate.pdf";
	}
}