package com.example.demo.Controllers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Course;
import com.example.demo.entity.CourseFeedback;
import com.example.demo.entity.Enrollment;
import com.example.demo.entity.Teacher;
import com.example.demo.entity.TeacherProfile;
import com.example.demo.enums.EnrollmentStatus;
import com.example.demo.repository.CourseFeedbackRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.NoteCategoryRepository;
import com.example.demo.repository.TeacherNotesRepository;
import com.example.demo.repository.TeacherProfileRepo;
import com.example.demo.repository.TeacherRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class TeacherController {

	@Autowired
	private TeacherRepository teacherRepo;

	@Autowired
	private TeacherProfileRepo teacherProfileRepo;

	@Autowired
	private EnrollmentRepository enrollmentRepo;

	@Autowired
	private CourseFeedbackRepository feedbackRepo;
	
	@Autowired
	private NoteCategoryRepository categoryRepo;
	
	@Autowired
	private TeacherNotesRepository teacherNoteRepo;

	// ===== DASHBOARD =====
	@GetMapping("/teacher-dashboard")
	public String dashboard(Model model, HttpSession session) {
//		Boolean loggedIn = (Boolean) session.getAttribute("TEACHER_LOGGED_IN");
//		Integer teacherId = (Integer) session.getAttribute("teacherId");

		Integer teacherId = 1; // remove this when testing done

//		if (loggedIn == null || !loggedIn || teacherId == null) {
//			return "redirect:/teacher-auth";
//		}

		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
		if (teacher == null) {
			session.invalidate();
			return "redirect:/teacher-auth";
		}

		model.addAttribute("teacher", teacher);
		return "teacher-dashboard";
	}

	// ===== STUDENTS =====
	@GetMapping("/teacher-students")
	public String teacherStudents(HttpSession session, Model model) {

		Integer teacherId = 1;
//		Integer teacherId = (Integer) session.getAttribute("teacherId");
//		if (teacherId == null) {
//			return "redirect:/teacher-auth";
//		}

		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
		if (teacher == null) {
			session.invalidate();
			return "redirect:/teacher-auth";
		}

		List<Enrollment> enrollments = enrollmentRepo.findByTeacherId(teacherId);

		model.addAttribute("teacher", teacher); // 🔥 THIS WAS MISSING
		model.addAttribute("enrollments", enrollments);

		return "teacher-students";
	}

	@PostMapping("/teacher/enrollment/status/{id}")
	public String updateEnrollmentStatus(@PathVariable Integer id, @RequestParam EnrollmentStatus status) {
		Enrollment e = enrollmentRepo.findById(id).orElseThrow();
		e.setStatus(status);
		enrollmentRepo.save(e);

		return "redirect:/teacher-students";
	}

	// ===== COMMUNICATION =====
	@GetMapping("/teacher-communication")
	public String communicationPage() {
		return "teacher-communication";
	}

	@GetMapping("/teacher-feedback")
	public String teacherFeedback(HttpSession session, Model model) {

		Integer teacherId = (Integer) session.getAttribute("teacherId");
		if (teacherId == null)
			return "redirect:/teacher-auth";

		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
		if (teacher == null) {
			session.invalidate();
			return "redirect:/teacher-auth";
		}

		List<CourseFeedback> feedbacks = feedbackRepo.findByTeacherId(teacherId);

		// group feedbacks by course
		Map<Course, List<CourseFeedback>> courseFeedbackMap = feedbacks.stream()
				.collect(Collectors.groupingBy(CourseFeedback::getCourse));

		// ⭐ NEW: calculate average rating per course
		Map<Integer, Double> avgRatingMap = courseFeedbackMap.entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey().getCourseId(),
						e -> e.getValue().stream().mapToInt(CourseFeedback::getRating).average().orElse(0.0)));

		model.addAttribute("courseFeedbackMap", courseFeedbackMap);
		model.addAttribute("avgRatingMap", avgRatingMap);
		model.addAttribute("teacher", teacher);

		return "teacher-feedback";
	}

	// ===== PROFILE =====
	@GetMapping("/teacher-profile")
	public String teacherProfile(HttpSession session, Model model) {

		 Boolean loggedIn = (Boolean) session.getAttribute("TEACHER_LOGGED_IN");
		 Integer teacherId = (Integer) session.getAttribute("teacherId");

		// Integer teacherId = 1; // remove this when testing done

		if (loggedIn == null || !loggedIn || teacherId == null) {
			return "redirect:/teacher-auth";
		}

		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
		if (teacher == null) {
			session.invalidate();
			return "redirect:/teacher-auth";
		}

		TeacherProfile profile = teacherProfileRepo.findByTeacherTeacherId(teacherId);

		if (profile == null) {
			profile = new TeacherProfile();
			profile.setTeacher(teacher);
		}

		model.addAttribute("teacher", teacher);
		model.addAttribute("profile", profile);

		return "teacher-profile";
	}

	@PostMapping("/teacher-profile")
	public String editTeacherProfile(@ModelAttribute("profile") TeacherProfile teacherProfile, HttpSession session) {

		// Integer teacherId = 1;// demo
		Boolean loggedIn = (Boolean) session.getAttribute("TEACHER_LOGGED_IN");
		Integer teacherId = (Integer) session.getAttribute("teacherId");

		if (loggedIn == null || !loggedIn || teacherId == null) {
			return "redirect:/teacher-auth";
		}

		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
		if (teacher == null) {
			session.invalidate();
			return "redirect:/teacher-auth";
		}

		TeacherProfile profile = teacherProfileRepo.findByTeacherTeacherId(teacherId);

		if (profile == null) {
			profile = new TeacherProfile();
			profile.setTeacher(teacher);
		}

		profile.setQualification(teacherProfile.getQualification());
		profile.setSpecialist(teacherProfile.getSpecialist());
		profile.setExperience(teacherProfile.getExperience());
		profile.setBio(teacherProfile.getBio());

		teacherProfileRepo.save(profile);

		return "redirect:/teacher-profile";
	}

	@PostMapping("/teacher-profile/upload-image")
	public String uploadTeacherProfileImage(@RequestParam("image") MultipartFile file, HttpSession session)
			throws Exception {

		Integer teacherId = (Integer) session.getAttribute("teacherId");
		if (teacherId == null)
			return "redirect:/teacher-auth";

		Teacher teacher = teacherRepo.findById(teacherId).orElseThrow();

		TeacherProfile profile = teacherProfileRepo.findByTeacherTeacherId(teacherId);

		if (profile == null) {
			profile = new TeacherProfile();
			profile.setTeacher(teacher);
		}

		String uploadDir = "uploads/teacher/";
		Files.createDirectories(Paths.get(uploadDir));

		String fileName = "teacher_" + teacherId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

		Path path = Paths.get(uploadDir + fileName);
		Files.write(path, file.getBytes());

		profile.setProfileImage("/" + uploadDir + fileName);
		teacherProfileRepo.save(profile);

		return "redirect:/teacher-profile";
	}

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/teacher-auth";
	}

	
	@GetMapping("/teacher-activity")
	public String teacherNotesAndAnnoucments(Model model,HttpSession session) {

		Integer teacherId =1;
//		Integer teacherId = (Integer) session.getAttribute("teacherId");
//		if (teacherId == null)
//			return "redirect:/teacher-auth";
		Teacher teacher = teacherRepo.findById(teacherId).orElseThrow();

		model.addAttribute("categories", categoryRepo.findByActiveTrue());
		model.addAttribute("notes", teacherNoteRepo.findByTeacherTeacherId(teacherId));
		model.addAttribute("teacher", teacher);
		return "teacher-activity";
	}
	
	@GetMapping("/test-teacher-login")
	public String testTeacherLogin(HttpSession session) {

		// simulate logged-in teacher (TESTING ONLY)
		session.setAttribute("TEACHER_LOGGED_IN", true);
		session.setAttribute("teacherId", 1); // must exist in DB
		session.setAttribute("teacherName", "Test Teacher");

		return "redirect:/teacher-dashboard";
	}
}
