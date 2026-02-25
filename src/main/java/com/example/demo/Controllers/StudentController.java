package com.example.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.entity.Course;
import com.example.demo.entity.CourseCertificate;
import com.example.demo.entity.CourseFeedback;
import com.example.demo.repository.CourseCertificateRepository;
import com.example.demo.repository.CourseFeedbackRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.StudentRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class StudentController {
	
	@Autowired
	private CourseCertificateRepository certificateRepo;
	
	@Autowired
	private EnrollmentRepository enrollmentRepo;
	
	@Autowired
	private CourseRepository courseRepo;
	
	@Autowired
	private CourseFeedbackRepository feedbackRepo;

	
	@Autowired
	private StudentRepository studentRepo;
	
	
	@GetMapping("/student-dashboard")
	public String student_dashboard() {

		return "student-dashboard";
	}

	@GetMapping("/student-learning")
	public String student_learning(HttpSession session, Model model) {

	    Integer studentId = (Integer) session.getAttribute("studentId");
	    if (studentId == null) {
	        return "redirect:/student-login";
	    }

	    // ✅ Certificates (already working)
	    List<CourseCertificate> certificates =
	            certificateRepo.findByStudentStudid(studentId);

	    // ✅ Completed courses (derived from certificates)
	    List<Course> completedCourses = certificates.stream()
	            .map(CourseCertificate::getCourse)
	            .distinct()
	            .toList();

	    model.addAttribute("certificates", certificates);
	    model.addAttribute("completedCourses", completedCourses);
	    model.addAttribute("feedbackGiven", false);
	    model.addAttribute("feedbacks", List.of());


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
	
	
	@GetMapping("/student-feedback")
	public String studentFeedback(HttpSession session, Model model) {

	    Integer studentId = (Integer) session.getAttribute("studentId");
	    if (studentId == null) return "redirect:/student-login";

	    List<CourseCertificate> certificates =
	        certificateRepo.findByStudentStudid(studentId);

	    List<Course> completedCourses = certificates.stream()
	        .map(CourseCertificate::getCourse)
	        .distinct()
	        .toList();

	    model.addAttribute("completedCourses", completedCourses);
	    model.addAttribute("courseSelected", false);
	    model.addAttribute("courseFeedbackSubmitted", false);

	    return "student-feedback";
	}	
	
	
	@GetMapping("/student-feedback/{courseId}")
	public String feedbackPage(
	        @PathVariable Integer courseId,
	        HttpSession session,
	        Model model) {

	    Integer studentId = (Integer) session.getAttribute("studentId");
	    if (studentId == null) return "redirect:/student-login";

	    boolean enrolled =
	        enrollmentRepo.existsByStudentStudidAndCourseCourseId(studentId, courseId);
	    if (!enrolled) return "redirect:/student-course-details?courseId=" + courseId;

	    Course course = courseRepo.findById(courseId).orElseThrow();

	    boolean alreadyGiven =
	        feedbackRepo.existsByCourseCourseIdAndStudentStudid(courseId, studentId);

	    model.addAttribute("course", course);
	    model.addAttribute("courseSelected", true);
	    model.addAttribute("courseFeedbackSubmitted", alreadyGiven);

	    return "student-feedback";
	}
	
	
	@PostMapping("/student-feedback/{courseId}")
	public String submitFeedback(
	        @PathVariable Integer courseId,
	        CourseFeedback feedback,
	        HttpSession session) {

	    Integer studentId = (Integer) session.getAttribute("studentId");
	    if (studentId == null) return "redirect:/student-login";

	    if (feedbackRepo.existsByCourseCourseIdAndStudentStudid(courseId, studentId)) {
	        return "redirect:/student-feedback/" + courseId;
	    }

	    feedback.setCourse(courseRepo.findById(courseId).orElseThrow());
	    feedback.setStudent(studentRepo.findById(studentId).orElseThrow());

	    feedbackRepo.save(feedback);

	    // 🔥 THIS IS KEY
	    return "redirect:/student-feedback/" + courseId;
	}

}
