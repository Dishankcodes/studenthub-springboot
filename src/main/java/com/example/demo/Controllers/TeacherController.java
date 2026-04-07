package com.example.demo.Controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Announcement;
import com.example.demo.entity.ChatUser;
import com.example.demo.entity.Course;
import com.example.demo.entity.CourseFeedback;
import com.example.demo.entity.Enrollment;
import com.example.demo.entity.Teacher;
import com.example.demo.entity.TeacherProfile;
import com.example.demo.enums.AnnouncementAudience;
import com.example.demo.enums.AnnouncementType;
import com.example.demo.enums.TeacherStatus;
import com.example.demo.enums.UserType;
import com.example.demo.repository.AnnouncementRepository;
import com.example.demo.repository.ChatUserRepository;
import com.example.demo.repository.CourseFeedbackRepository;
import com.example.demo.repository.CourseRepository;
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

	@Autowired
	private AnnouncementRepository announcementRepo;

	@Autowired
	private CourseRepository courseRepo;

	
	@Autowired
	private ChatUserRepository chatUserRepo;

	
	private boolean isBlocked(Teacher teacher) {
		return teacher.getStatus() == TeacherStatus.BLOCKED;
	}

	private boolean isSuspended(Teacher teacher) {
		return teacher.getStatus() == TeacherStatus.SUSPENDED;
	}

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

	
		Map<Integer, Integer> chatUserMap = new HashMap<>();

		for (var e : enrollments) {

		    ChatUser u = chatUserRepo
		        .findByRefIdAndType(e.getStudent().getStudid(), UserType.STUDENT)
		        .orElseGet(() -> {
		            ChatUser newUser = new ChatUser();
		            newUser.setRefId(e.getStudent().getStudid());
		            newUser.setType(UserType.STUDENT);
		            return chatUserRepo.save(newUser);
		        });

		    chatUserMap.put(e.getStudent().getStudid(), u.getId());
		}

		model.addAttribute("chatUserMap", chatUserMap);
		model.addAttribute("teacher", teacher);
		model.addAttribute("enrollments", enrollments);

		return "teacher-students";
	}


	@GetMapping("/teacher-feedback")
	public String teacherFeedback(HttpSession session, Model model) {

//		Integer teacherId = (Integer) session.getAttribute("teacherId");
//		if (teacherId == null)
//			return "redirect:/teacher-auth";

		Integer teacherId = 1;
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

		Integer teacherId = 1;// demo
//		Boolean loggedIn = (Boolean) session.getAttribute("TEACHER_LOGGED_IN");
//		Integer teacherId = (Integer) session.getAttribute("teacherId");
//
//		if (loggedIn == null || !loggedIn || teacherId == null) {
//			return "redirect:/teacher-auth";
//		}

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
//
//		Integer teacherId = (Integer) session.getAttribute("teacherId");
//		if (teacherId == null)
//			return "redirect:/teacher-auth";
		Integer teacherId = 1;
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
	public String teacherNotesAndAnnoucments(Model model, HttpSession session) {

		Integer teacherId = 1;
//		Integer teacherId = (Integer) session.getAttribute("teacherId");
//		if (teacherId == null)
//			return "redirect:/teacher-auth";
		Teacher teacher = teacherRepo.findById(teacherId).orElseThrow();

		model.addAttribute("myAnnouncements",
				announcementRepo.findByTeacherTeacherIdAndActiveTrueOrderByCreatedAtDesc(teacherId));

		model.addAttribute("adminAnnouncements",
				announcementRepo.findByTeacherIsNullAndActiveTrueOrderByCreatedAtDesc());
		model.addAttribute("allAnnouncements", announcementRepo.findForTeachers());
		model.addAttribute("categories", categoryRepo.findByActiveTrue());
		model.addAttribute("notes", teacherNoteRepo.findByTeacherTeacherId(teacherId));
		model.addAttribute("teacher", teacher);
		return "teacher-activity";
	}

	@PostMapping("/teacher-announcement/create")
	public String createTeacherAnnouncement(@RequestParam(required = false) Integer courseId,
			@RequestParam String title, @RequestParam String message,
			@RequestParam(required = false) MultipartFile file, HttpSession session) throws IOException {

		Integer teacherId = 1;
		Teacher teacher = teacherRepo.findById(teacherId).orElseThrow();

		if (isBlocked(teacher)) {
			return "redirect:/teacher-activity?error=blocked";
		}

		if (isSuspended(teacher)) {
			return "redirect:/teacher-activity?error=suspended";
		}

		Announcement a = new Announcement();
		a.setTitle(title);
		a.setMessage(message);
		a.setTeacher(teacher);
		a.setType(AnnouncementType.GENERAL);
		a.setAudience(AnnouncementAudience.STUDENTS);

		if (courseId != null) {
			Course course = courseRepo.findById(courseId).orElse(null);
			a.setCourse(course);
		}

		if (file != null && !file.isEmpty()) {

			String dir = System.getProperty("user.dir") + "/uploads/announcements/";
			Files.createDirectories(Paths.get(dir));

			String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
			file.transferTo(new File(dir + fileName));

			a.setAttachmentUrl("/uploads/announcements/" + fileName);
			a.setAttachmentName(file.getOriginalFilename());
		}

		announcementRepo.save(a);

		return "redirect:/teacher-activity?created";
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
