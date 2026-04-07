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

import com.example.demo.entity.ChatUser;
import com.example.demo.entity.Course;
import com.example.demo.entity.CourseCertificate;
import com.example.demo.entity.Enrollment;
import com.example.demo.entity.InstructorFeedback;
import com.example.demo.entity.InternshipApplication;
import com.example.demo.entity.Student;
import com.example.demo.entity.Teacher;
import com.example.demo.entity.TeacherProfile;
import com.example.demo.enums.CourseStatus;
import com.example.demo.enums.TeacherStatus;
import com.example.demo.enums.UserType;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.ChatUserRepository;
import com.example.demo.repository.CourseCertificateRepository;
import com.example.demo.repository.CourseFeedbackRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.InstructorFeedbackRepository;
import com.example.demo.repository.InternshipRepository;
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

	@Autowired
	private ChatUserRepository chatUserRepo;

	@Autowired
	private CourseRepository courseRepo;

	@Autowired
	private InternshipRepository internshipRepo;

	@Autowired
	private CourseFeedbackRepository feedbackRepo;

	@GetMapping("/admin-dashboard")
	public String admin_dashboard(HttpSession session, Model model) {

		if (session.getAttribute("adminEmail") == null) {
			return "redirect:/admin-login";
		}

		String username = (String) session.getAttribute("adminUsername");

		// ===== COUNTS =====
		long totalStudents = studentRepo.count();
		long totalTeachers = teacherRepo.count();
		long totalCourses = courseRepo.count();
		long totalInternships = internshipRepo.count();
		long totalEnrollments = enrollmentRepo.count();
		long totalApplications = applicationRepo.count();
		long totalFeedback = feedbackRepo.count();

		// ===== SEND =====
		model.addAttribute("username", username);
		model.addAttribute("totalStudents", totalStudents);
		model.addAttribute("totalTeachers", totalTeachers);
		model.addAttribute("totalCourses", totalCourses);
		model.addAttribute("totalInternships", totalInternships);
		model.addAttribute("totalEnrollments", totalEnrollments);
		model.addAttribute("totalApplications", totalApplications);
		model.addAttribute("totalFeedback", totalFeedback);

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

		studentRepo.deleteById(studId);

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

		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);

		if (teacher == null) {
			return "redirect:/manage-teachers";
		}

		TeacherProfile profile = teacherProfileRepo.findByTeacherTeacherId(teacherId);

		List<InstructorFeedback> feedbacks = instructorFeedbackRepo.findByTeacherTeacherId(teacherId);

		double avgRating = instructorFeedbackRepo.getAverageRating(teacherId);
		Long totalRatings = instructorFeedbackRepo.getTotalRatings(teacherId);

		if (avgRating == 0)
			avgRating = 0.0;
		if (totalRatings == null)
			totalRatings = 0L;

		// 🔥 COURSES
		List<Course> courses = courseRepo.findByTeacherTeacherIdAndStatusNot(teacherId, CourseStatus.DELETED);

		if (courses == null)
			courses = List.of();

		long totalCourses = courses.size();

		long publishedCourses = courses.stream().filter(c -> c.getStatus() == CourseStatus.PUBLISHED).count();

		Map<Integer, Long> courseStudentCount = new HashMap<>();

		for (Course c : courses) {
			long count = (c.getEnrollments() != null) ? c.getEnrollments().size() : 0;

			courseStudentCount.put(c.getCourseId(), count);
		}

		ChatUser chatUser = chatUserRepo.findByRefIdAndType(teacherId, UserType.TEACHER).orElse(null);

		model.addAttribute("chatUser", chatUser);
		model.addAttribute("teacher", teacher);
		model.addAttribute("profile", profile);
		model.addAttribute("feedbacks", feedbacks != null ? feedbacks : List.of());
		model.addAttribute("avgRating", avgRating);
		model.addAttribute("totalRatings", totalRatings);
		model.addAttribute("courses", courses);
		model.addAttribute("totalCourses", totalCourses);
		model.addAttribute("publishedCourses", publishedCourses);
		model.addAttribute("courseStudentCount", courseStudentCount);

		model.addAttribute("username", session.getAttribute("adminUsername"));

		return "admin-instructor-view";
	}

	@PostMapping("/manage-instructor/status/{id}")
	public String updateTeacherStatus(@PathVariable Integer id, @RequestParam TeacherStatus status,
			RedirectAttributes ra) {

		Teacher teacher = teacherRepo.findById(id).orElse(null);

		if (teacher == null) {
			return "redirect:/manage-teachers";
		}

		teacher.setStatus(status);
		teacherRepo.save(teacher);

		String name = teacher.getFirstname() + " " + teacher.getLastname();

		ra.addFlashAttribute("statusMessage", name + " is now " + status.name());
		ra.addFlashAttribute("highlightId", id);

		return "redirect:/manage-teachers";
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
		List<InternshipApplication> apps = applicationRepo.findByStudent_Studid(id);

		long totalInternships = apps.size();

		long completedInternships = apps.stream().filter(a -> a.getStatus().name().equals("SELECTED")).count();

		long acceptedInternships = apps.stream().filter(a -> a.getStatus().name().equals("ACCEPTED")).count();

		List<Enrollment> enrollments = enrollmentRepo.findByStudentStudid(id);

		Map<Integer, Long> courseDurationMap = new HashMap<>();

		for (Enrollment e : enrollments) {

			if (e.getCompletedAt() != null && e.getEnrolledAt() != null) {

				long days = ChronoUnit.DAYS.between(e.getEnrolledAt(), e.getCompletedAt());

				courseDurationMap.put(e.getCourse().getCourseId(), days);
			}
		}

		model.addAttribute("enrollments", enrollments);
		model.addAttribute("courseDurationMap", courseDurationMap);
		long enrolledCourses = enrollments.size();

		long completedCourses = enrollments.stream().filter(e -> e.getCompletedAt() != null).count();

		// ===== CERTIFICATES =====
		List<CourseCertificate> certificates = certificateRepo.findByStudentStudid(id);

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

	@GetMapping("/admin/view-profile")
	public String viewUserProfile(@RequestParam Integer userId, Model model) {

		// 🔥 Get ChatUser
		ChatUser user = chatUserRepo.findById(userId).orElse(null);

		if (user == null) {
			return "redirect:/admin-chat";
		}

		// ✅ IF STUDENT
		if (user.getType() == UserType.STUDENT) {

			Integer studentId = user.getRefId();

			return "redirect:/admin-student-dashboard?id=" + studentId;
		}

		// ✅ IF TEACHER
		else if (user.getType() == UserType.TEACHER) {

			Integer teacherId = user.getRefId();

			return "redirect:/admin-instructor-view/" + teacherId;
		}

		return "redirect:/admin-chat";
	}
}
