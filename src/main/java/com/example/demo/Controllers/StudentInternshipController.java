package com.example.demo.Controllers;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.InternshipApplication;
import com.example.demo.entity.Internships;
import com.example.demo.entity.Student;
import com.example.demo.enums.ApplicationStatus;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.InternshipRepository;
import com.example.demo.repository.StudentRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class StudentInternshipController {

	@Autowired
	private InternshipRepository internshipRepo;

	@Autowired
	private ApplicationRepository applicationRepo;

	@Autowired
	private StudentRepository studentRepo;

	@Autowired
	private EnrollmentRepository enrollmentRepo;

	
	@GetMapping("/student-internships")
	public String studentInternships(Model model, HttpSession session) {

		Integer studentId = (Integer) session.getAttribute("studentId");

		if (studentId == null) {
			return "redirect:/student-login";
		}

		List<Internships> internships = internshipRepo.findAll();

		// ✅ FIXED METHOD HERE
		List<InternshipApplication> applications = applicationRepo.findByStudent_Studid(studentId);

		Map<Integer, InternshipApplication> appMap = new HashMap<>();

		for (InternshipApplication app : applications) {
			appMap.put(app.getInternship().getId(), app);
		}

		model.addAttribute("internships", internships);
		model.addAttribute("appMap", appMap);

		return "student-internships";
	}

	@GetMapping("/student-internship-detail")
	public String internshipDetail(@RequestParam Integer id, Model model, HttpSession session) {

		Integer studentId = (Integer) session.getAttribute("studentId");

		if (studentId == null)
			return "redirect:/student-login";

		Internships internship = internshipRepo.findById(id).orElse(null);

		InternshipApplication application = null;

		if (studentId != null) {
			application = applicationRepo.findByStudent_StudidAndInternship_Id(studentId, id).orElse(null);
		}

		
		model.addAttribute("internship", internship);
		model.addAttribute("application", application);

		return "student-internship-detail";
	}

	@PostMapping("/student/apply")
	public String applyInternship(@RequestParam Integer internshipId, @RequestParam String fullName,
			@RequestParam String email, @RequestParam String phone, @RequestParam(required = false) String coverLetter,
			@RequestParam(required = false) MultipartFile resume, RedirectAttributes redirectAttributes,
			HttpSession session) throws Exception {

		Integer studentId = (Integer) session.getAttribute("studentId");

		if (studentId == null) {
			return "redirect:/student-login";
		}

		Internships internship = internshipRepo.findById(internshipId).orElse(null);

		Student student = studentRepo.findById(studentId).orElse(null);

		// already applied
		if (applicationRepo.existsByStudent_StudidAndInternship_Id(studentId, internshipId)) {
			redirectAttributes.addFlashAttribute("error", "Already applied.");
			return "redirect:/student-internship-detail?id=" + internshipId;
		}

		// course eligibility
		if (internship.getRequiredCourse() != null) {

			List<Integer> completedCourses = enrollmentRepo.findCompletedCourses(studentId);

			if (!completedCourses.contains(internship.getRequiredCourse().getCourseId())) {
				redirectAttributes.addFlashAttribute("error", "Complete required course first.");
				return "redirect:/student-internship-detail?id=" + internshipId;
			}
		}

		InternshipApplication app = new InternshipApplication();

		app.setStudent(student);
		app.setInternship(internship);
		app.setStatus(ApplicationStatus.PENDING);

		app.setFullName(fullName);
		app.setEmail(email);
		app.setPhone(phone);
		app.setCoverLetter(coverLetter);

		if (resume != null && !resume.isEmpty()) {

			String basePath = System.getProperty("user.dir") + "/uploads/resumes/" + studentId;

			File dir = new File(basePath);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			String fileName = System.currentTimeMillis() + "_" + resume.getOriginalFilename();

			File destination = new File(dir, fileName);
			resume.transferTo(destination);

			app.setResumeUrl("/uploads/resumes/" + studentId + "/" + fileName);
		}

		applicationRepo.save(app);

		redirectAttributes.addFlashAttribute("success", "Applied Successfully 🚀");

		return "redirect:/student-internship-detail?id=" + internshipId;
	}
}