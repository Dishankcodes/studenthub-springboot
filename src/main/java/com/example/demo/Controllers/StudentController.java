package com.example.demo.Controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.entity.Course;
import com.example.demo.entity.CourseCertificate;
import com.example.demo.entity.CourseFeedback;
import com.example.demo.entity.InstructorFeedback;
import com.example.demo.entity.Teacher;
import com.example.demo.entity.TeacherNotes;
import com.example.demo.repository.CourseCertificateRepository;
import com.example.demo.repository.CourseFeedbackRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.InstructorFeedbackRepository;
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
		List<CourseCertificate> certificates = certificateRepo.findByStudentStudid(studentId);

		// ✅ Completed courses (derived from certificates)
		List<Course> completedCourses = certificates.stream().map(CourseCertificate::getCourse).distinct().toList();

		// ✅ FEEDBACK STATUS PER COURSE
		Map<Integer, Boolean> courseFeedbackMap = new HashMap<>();

		for (Course course : completedCourses) {
			boolean feedbackGiven = feedbackRepo.existsByCourseCourseIdAndStudentStudid(course.getCourseId(),
					studentId);
			courseFeedbackMap.put(course.getCourseId(), feedbackGiven);
		}

		model.addAttribute("courseFeedbackMap", courseFeedbackMap);
		model.addAttribute("certificates", certificates);
		model.addAttribute("completedCourses", completedCourses);

		return "student-learning";
	}

	@GetMapping("/student-stuff")
	public String student_stuff(Model model) {

		List<TeacherNotes> notes = teacherNoteRepo.findByApprovedTrueOrderByUploadedAtDesc();

		model.addAttribute("notes", notes);
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

	@GetMapping("/student-feedback")
	public String studentFeedback(HttpSession session, Model model) {

		Integer studentId = (Integer) session.getAttribute("studentId");
		if (studentId == null)
			return "redirect:/student-login";

		List<CourseCertificate> certificates = certificateRepo.findByStudentStudid(studentId);

		List<Course> completedCourses = certificates.stream().map(CourseCertificate::getCourse).distinct().toList();

		List<Teacher> completedTeachers = certificates.stream().map(cc -> cc.getCourse().getTeacher()).distinct()
				.toList();

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

		// 🔥 THIS IS KEY
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
		model.addAttribute("courseSelected", false); // important

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

		List<CourseCertificate> certs = certificateRepo.findByStudentStudid(studentId);

		return certs.stream().anyMatch(c -> c.getCourse().getTeacher().getTeacherId().equals(teacherId));
	}

}
