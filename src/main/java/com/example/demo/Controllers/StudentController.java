package com.example.demo.Controllers;

import java.util.ArrayList;
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

import com.example.demo.entity.Announcement;
import com.example.demo.entity.ChatUser;
import com.example.demo.entity.Connection;
import com.example.demo.entity.Course;
import com.example.demo.entity.CourseCertificate;
import com.example.demo.entity.CourseFeedback;
import com.example.demo.entity.Enrollment;
import com.example.demo.entity.InstructorFeedback;
import com.example.demo.entity.Teacher;
import com.example.demo.entity.TeacherNotes;
import com.example.demo.enums.ConnectionStatus;
import com.example.demo.enums.CourseStatus;
import com.example.demo.repository.AnnouncementRepository;
import com.example.demo.repository.ChatUserRepository;
import com.example.demo.repository.ConnectionRepository;
import com.example.demo.repository.CourseCertificateRepository;
import com.example.demo.repository.CourseFeedbackRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.InstructorFeedbackRepository;
import com.example.demo.repository.LessonProgressRepository;
import com.example.demo.repository.NoteCategoryRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TeacherNotesRepository;
import com.example.demo.repository.TeacherRepository;

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

	@Autowired
	private InstructorFeedbackRepository instructorFeedbackRepo;

	@Autowired
	private TeacherRepository teacherRepo;

	@Autowired
	private TeacherNotesRepository teacherNoteRepo;

	@Autowired
	private NoteCategoryRepository categoryRepo;

	@Autowired
	private AnnouncementRepository announcementRepo;

	@Autowired
	private LessonProgressRepository lessonProgressRepo;

	@Autowired
	private ConnectionRepository connectionRepo;
	
	@Autowired
	private ChatUserRepository chatUserRepo;
	
	@GetMapping("/student-dashboard")
	public String student_dashboard(HttpSession session, Model model) {

		Integer studentId = (Integer) session.getAttribute("studentId");

		if (studentId == null) {
			return "redirect:/student-login";
		}

		// ===== ENROLLMENTS =====
		List<Enrollment> enrollments = enrollmentRepo.findByStudentStudid(studentId);

		List<Course> enrolledCourses = enrollments.stream().map(Enrollment::getCourse).toList();

		List<Course> completedCourses = enrollments.stream().filter(e -> e.getCompletedAt() != null)
				.map(Enrollment::getCourse).toList();

		// ===== IN PROGRESS COURSES =====
		List<Course> inProgressCourses = enrollments.stream().filter(e -> e.getCompletedAt() == null)
				.map(Enrollment::getCourse).toList();

		if (inProgressCourses.size() > 4) {
			inProgressCourses = inProgressCourses.subList(0, 4);
		}

		// ===== CERTIFICATES =====
		List<CourseCertificate> certificates = certificateRepo.findByStudentStudid(studentId);

		// ===== ANNOUNCEMENTS =====
		List<Announcement> announcements = announcementRepo.findTop5ByActiveTrueOrderByCreatedAtDesc();

		// ===== CLASSROOM NOTES =====
		List<TeacherNotes> notes = teacherNoteRepo.findTop5ByApprovedTrueOrderByUploadedAtDesc();

		// ===== DASHBOARD COUNTS =====
		int enrolledCount = enrolledCourses.size();
		int completedCount = completedCourses.size();
		int certificateCount = certificates.size();
		int inProgressCount = inProgressCourses.size();

		// ===== WEEKLY ACTIVITY ====
		List<Long> weeklyActivity = lessonProgressRepo.getWeeklyActivity(studentId);

		if (weeklyActivity == null || weeklyActivity.size() != 7) {
			weeklyActivity = List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L);
		}

		
		ChatUser me = chatUserRepo
		        .findByRefIdAndType(studentId, com.example.demo.enums.UserType.STUDENT)
		        .orElse(null);

		List<ChatUser> list = new ArrayList<>();

		if (me != null) {

		    List<Connection> connections =
		            connectionRepo.findBySenderIdAndStatusOrReceiverIdAndStatus(
		                    me.getId(), ConnectionStatus.ACCEPTED,
		                    me.getId(), ConnectionStatus.ACCEPTED
		            );

		    for (Connection c : connections) {

		        ChatUser other = c.getSender().getId().equals(me.getId())
		                ? c.getReceiver()
		                : c.getSender();

		        list.add(other);
		    }
		}
		
		
		model.addAttribute("enrolledCount", enrolledCount);
		model.addAttribute("completedCount", completedCount);
		model.addAttribute("certificateCount", certificateCount);
		model.addAttribute("inProgressCount", inProgressCount);
		model.addAttribute("continueCourses", inProgressCourses);
		model.addAttribute("announcements", announcements);
		model.addAttribute("recentNotes", notes);
		model.addAttribute("certificates", certificates);

		model.addAttribute("weeklyActivity", weeklyActivity);

		return "student-dashboard";
	}

	@GetMapping("/student-learning")
	public String student_learning(HttpSession session, Model model) {

		Integer studentId = (Integer) session.getAttribute("studentId");

		if (studentId == null) {
			return "redirect:/student-login";
		}

		List<Enrollment> enrollments = enrollmentRepo.findByStudentStudid(studentId);

		List<Course> completedCourses = enrollments.stream().filter(e -> e.getCompletedAt() != null)
				.map(Enrollment::getCourse).toList();

		List<CourseCertificate> certificates = certificateRepo.findByStudentStudid(studentId);

		Map<Integer, Boolean> courseFeedbackMap = new HashMap<>();

		for (Course course : completedCourses) {

			boolean feedbackGiven = feedbackRepo.existsByCourseCourseIdAndStudentStudid(course.getCourseId(),
					studentId);

			courseFeedbackMap.put(course.getCourseId(), feedbackGiven);
		}

		List<Course> recommendedCourses = courseRepo.findByStatus(CourseStatus.PUBLISHED);

		recommendedCourses.removeIf(course -> completedCourses.contains(course));

		if (recommendedCourses.size() > 6) {
			recommendedCourses = recommendedCourses.subList(0, 6);
		}

		model.addAttribute("recommendedCourses", recommendedCourses);
		model.addAttribute("courseFeedbackMap", courseFeedbackMap);
		model.addAttribute("certificates", certificates);
		model.addAttribute("completedCourses", completedCourses);

		return "student-learning";
	}

	@GetMapping("/student-stuff")
	public String student_stuff(@RequestParam(required = false) Integer category,
			@RequestParam(required = false) String q, Model model) {

		model.addAttribute("categories", categoryRepo.findByActiveTrue());

		List<TeacherNotes> notes = teacherNoteRepo.search(category, q);

		boolean hasAnyNotes = teacherNoteRepo.countApprovedNotes() > 0;

		List<Announcement> announcements = announcementRepo.findForStudents();

		model.addAttribute("notes", notes);
		model.addAttribute("hasAnyNotes", hasAnyNotes);
		model.addAttribute("announcements", announcements);
		model.addAttribute("selectedCategory", category);
		model.addAttribute("q", q);

		return "student-stuff";
	}

	

	@GetMapping("/student-feedback")
	public String studentFeedback(HttpSession session, Model model) {

		Integer studentId = (Integer) session.getAttribute("studentId");

		if (studentId == null)
			return "redirect:/student-login";

		List<Enrollment> enrollments = enrollmentRepo.findByStudentStudid(studentId);

		List<Course> completedCourses = enrollments.stream().filter(e -> e.getCompletedAt() != null)
				.map(Enrollment::getCourse).toList();

		List<Teacher> completedTeachers = enrollments.stream().filter(e -> e.getCompletedAt() != null)
				.map(e -> e.getCourse().getTeacher()).distinct().toList();

		model.addAttribute("feedbackMode", "LIST");
		model.addAttribute("completedTeachers", completedTeachers);
		model.addAttribute("completedCourses", completedCourses);
		model.addAttribute("courseSelected", false);
		model.addAttribute("courseFeedbackSubmitted", false);
		model.addAttribute("canRateInstructor", false);
		model.addAttribute("instructorFeedbackGiven", false);
		model.addAttribute("teacher", null);

		return "student-feedback";
	}

	@GetMapping("/student-feedback/{courseId}")
	public String feedbackPage(@PathVariable Integer courseId, HttpSession session, Model model) {

		Integer studentId = (Integer) session.getAttribute("studentId");

		if (studentId == null)
			return "redirect:/student-login";

		boolean enrolled = enrollmentRepo.existsByStudentStudidAndCourseCourseId(studentId, courseId);

		if (!enrolled)
			return "redirect:/student-course-details?courseId=" + courseId;

		Course course = courseRepo.findById(courseId).orElseThrow();

		Teacher teacher = course.getTeacher();

		boolean courseFeedbackGiven = feedbackRepo.existsByCourseCourseIdAndStudentStudid(courseId, studentId);

		boolean canRateInstructor = canRateInstructor(studentId, teacher.getTeacherId());

		boolean instructorFeedbackGiven = instructorFeedbackRepo
				.existsByTeacherTeacherIdAndStudentStudid(teacher.getTeacherId(), studentId);

		model.addAttribute("feedbackMode", "DIRECT");
		model.addAttribute("course", course);
		model.addAttribute("teacher", teacher);
		model.addAttribute("courseSelected", true);
		model.addAttribute("courseFeedbackSubmitted", courseFeedbackGiven);
		model.addAttribute("canRateInstructor", canRateInstructor);
		model.addAttribute("instructorFeedbackGiven", instructorFeedbackGiven);

		return "student-feedback";
	}

	@PostMapping("/student-feedback/{courseId}")
	public String submitFeedback(@PathVariable Integer courseId, CourseFeedback feedback, HttpSession session) {

		Integer studentId = (Integer) session.getAttribute("studentId");

		if (studentId == null)
			return "redirect:/student-login";

		if (feedbackRepo.existsByCourseCourseIdAndStudentStudid(courseId, studentId)) {
			return "redirect:/student-feedback/" + courseId;
		}

		feedback.setCourse(courseRepo.findById(courseId).orElseThrow());

		feedback.setStudent(studentRepo.findById(studentId).orElseThrow());

		feedbackRepo.save(feedback);

		return "redirect:/student-feedback/" + courseId;
	}

	@GetMapping("/student-feedback/instructor/{teacherId}")
	public String instructorFeedbackPage(@PathVariable Integer teacherId, HttpSession session, Model model) {

		Integer studentId = (Integer) session.getAttribute("studentId");

		if (studentId == null)
			return "redirect:/student-login";

		Teacher teacher = teacherRepo.findById(teacherId).orElseThrow();

		boolean canRateInstructor = canRateInstructor(studentId, teacherId);

		boolean instructorFeedbackGiven = instructorFeedbackRepo.existsByTeacherTeacherIdAndStudentStudid(teacherId,
				studentId);

		model.addAttribute("feedbackMode", "DIRECT");
		model.addAttribute("teacher", teacher);
		model.addAttribute("canRateInstructor", canRateInstructor);
		model.addAttribute("instructorFeedbackGiven", instructorFeedbackGiven);
		model.addAttribute("courseSelected", false);

		return "student-feedback";
	}

	@PostMapping("/student-feedback/instructor/{teacherId}")
	public String submitInstructorFeedback(@PathVariable Integer teacherId, InstructorFeedback feedback,
			HttpSession session) {

		Integer studentId = (Integer) session.getAttribute("studentId");

		if (studentId == null)
			return "redirect:/student-login";

		if (instructorFeedbackRepo.existsByTeacherTeacherIdAndStudentStudid(teacherId, studentId)) {
			return "redirect:/student-feedback";
		}

		feedback.setTeacher(teacherRepo.findById(teacherId).orElseThrow());

		feedback.setStudent(studentRepo.findById(studentId).orElseThrow());

		instructorFeedbackRepo.save(feedback);

		return "redirect:/student-feedback";
	}

	private boolean canRateInstructor(Integer studentId, Integer teacherId) {

		List<Enrollment> enrollments = enrollmentRepo.findByStudentStudid(studentId);

		return enrollments.stream().filter(e -> e.getCompletedAt() != null)
				.anyMatch(e -> e.getCourse().getTeacher().getTeacherId().equals(teacherId));
	}
}