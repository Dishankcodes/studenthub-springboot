package com.example.demo.Controllers;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.CourseCertificate;
import com.example.demo.entity.Enrollment;
import com.example.demo.entity.InstructorFeedback;
import com.example.demo.entity.InternshipApplication;
import com.example.demo.entity.Student;
import com.example.demo.entity.Teacher;
import com.example.demo.entity.TeacherProfile;
import com.example.demo.enums.TeacherStatus;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.CourseCertificateRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.InstructorFeedbackRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TeacherProfileRepo;
import com.example.demo.repository.TeacherRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

	@Autowired
	private StudentRepository studentRepo;

	@Autowired
	private TeacherRepository teacherRepo;

	@Autowired
	private TeacherProfileRepo teacherProfileRepo;

	@Autowired
	private InstructorFeedbackRepository instructorFeedbackRepo;
	
	@Autowired
	private CourseCertificateRepository certificateRepo;

	@Autowired
	private EnrollmentRepository enrollmentRepo;
	
	@Autowired
	private ApplicationRepository applicationRepo;


	@GetMapping("/admin-dashboard")
	public String admin_dashboard(HttpSession session, Model model) {
		if (session.getAttribute("adminEmail") == null) {
			return "redirect:/admin-login";
		}

		String username = (String) session.getAttribute("adminUsername");
		model.addAttribute("username", username);
		return "admin-dashboard";
	}

	@GetMapping("/manage-students")
	public String admin_student(HttpSession session, Model model) {

		if (session.getAttribute("adminEmail") == null) {
			return "redirect:/admin-login";
		}
		List<Student> students = studentRepo.findAll();
		model.addAttribute("students", students);

		String username = (String) session.getAttribute("adminUsername");
		model.addAttribute("username", username);

		return "manage-students";
	}

	@PostMapping("/admin-student/delete/{studId}")
	public String deleteStudent(@PathVariable Integer studId, RedirectAttributes ra, HttpSession session) {

		if (session.getAttribute("adminEmail") == null) {
			return "redirect:/admin-login";
		}

		studentRepo.deleteById(studId); // 🔥 cascades automatically

		ra.addFlashAttribute("success", "✅ Student deleted permanently");

		return "redirect:/manage-students";
	}

	@GetMapping("/manage-teachers")
	public String admin_instructor(HttpSession session, Model model) {

		if (session.getAttribute("adminEmail") == null) {
			return "redirect:/admin-login";
		}

		List<Teacher> teachers = teacherRepo.findAll();
		model.addAttribute("teachers", teachers);

		String username = (String) session.getAttribute("adminUsername");
		model.addAttribute("username", username);

		return "manage-instructor";
	}

	@GetMapping("/admin-instructor-view/{teacherId}")
	public String viewInstructorAndFeedback(@PathVariable Integer teacherId, Model model, HttpSession session) {

		if (session.getAttribute("adminEmail") == null) {
			return "redirect:/admin-login";
		}

		Teacher teacher = teacherRepo.findById(teacherId).orElseThrow();
		TeacherProfile profile = teacherProfileRepo.findByTeacherTeacherId(teacherId);

		List<InstructorFeedback> feedbacks = instructorFeedbackRepo.findByTeacherTeacherId(teacherId);

		double avgRating = instructorFeedbackRepo.getAverageRating(teacherId);

		Long totalRatings = instructorFeedbackRepo.getTotalRatings(teacherId);

		model.addAttribute("teacher", teacher);
		model.addAttribute("profile", profile);
		model.addAttribute("feedbacks", feedbacks);
		model.addAttribute("avgRating", avgRating);
		model.addAttribute("totalRatings", totalRatings);
		model.addAttribute("username", session.getAttribute("adminUsername"));

		return "admin-instructor-view";
	}

	@PostMapping("/manage-instructor/status/{id}")
	public String updateTeacherStatus(@PathVariable Integer id, @RequestParam TeacherStatus status) {
		Teacher teacher = teacherRepo.findById(id).orElse(null);

		if (teacher == null) {
			return "redirect:/manage-instructor";
		}

		teacher.setStatus(status);
		teacherRepo.save(teacher);

		return "redirect:/manage-teachers#instructor-" + id;
	}

	@GetMapping("/admin-feedback")
	public String admin_feedback() {

		return "admin-feedback";
	}

	@GetMapping("/admin-settings")
	public String admin_settings() {

		return "admin-settings";
	}

	
	
	@GetMapping("/admin-logout")
	public String adminLogout(HttpSession session) {
		session.invalidate();
		return "redirect:/admin-login";
	}
	
	@GetMapping("/admin-student-dashboard")
	public String studentDashboard(@RequestParam Integer id, Model model) {

	    Student student = studentRepo.findById(id).orElse(null);

	    if (student == null) {
	        model.addAttribute("error", "Student not found");
	        return "admin-student-dashboard";
	    }

	    // ===== INTERNSHIPS =====
	    List<InternshipApplication> apps =
	            applicationRepo.findByStudent_Studid(id);

	    long totalInternships = apps.size();

	    long completedInternships = apps.stream()
	            .filter(a -> a.getStatus().name().equals("SELECTED"))
	            .count();

	    long acceptedInternships = apps.stream()
	            .filter(a -> a.getStatus().name().equals("ACCEPTED"))
	            .count();

	   
	    List<Enrollment> enrollments = enrollmentRepo.findByStudentStudid(id);

	  
	    Map<Integer, Long> courseDurationMap = new HashMap<>();

	    for (Enrollment e : enrollments) {

	        if (e.getCompletedAt() != null && e.getEnrolledAt() != null) {

	            long days = ChronoUnit.DAYS.between(
	                    e.getEnrolledAt(),
	                    e.getCompletedAt()
	            );

	            courseDurationMap.put(e.getCourse().getCourseId(), days);
	        }
	    }

	    model.addAttribute("enrollments", enrollments);
	    model.addAttribute("courseDurationMap", courseDurationMap);
	    long enrolledCourses = enrollments.size();

	    long completedCourses = enrollments.stream()
	            .filter(e -> e.getCompletedAt() != null)
	            .count();

	    // ===== CERTIFICATES =====
	    List<CourseCertificate> certificates =
	            certificateRepo.findByStudentStudid(id);

	    long certificateCount = certificates.size();

	    // ===== MODEL =====
	    model.addAttribute("student", student);
	    model.addAttribute("applications", apps);

	    model.addAttribute("totalInternships", totalInternships);
	    model.addAttribute("completedInternships", completedInternships);
	    model.addAttribute("acceptedInternships", acceptedInternships);

	    model.addAttribute("enrolledCourses", enrolledCourses);
	    model.addAttribute("completedCourses", completedCourses);
	    model.addAttribute("certificateCount", certificateCount);

	    return "admin-student-dashboard";
	}
}
