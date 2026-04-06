package com.example.demo.Controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.ChatUser;
import com.example.demo.entity.Course;
import com.example.demo.entity.InstructorFeedback;
import com.example.demo.entity.Student;
import com.example.demo.entity.Teacher;
import com.example.demo.entity.TeacherProfile;
import com.example.demo.enums.CourseStatus;
import com.example.demo.enums.UserType;
import com.example.demo.repository.ChatUserRepository;
import com.example.demo.repository.ConnectionRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.InstructorFeedbackRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TeacherProfileRepo;
import com.example.demo.repository.TeacherRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class StudentViewProfileController {

	@Autowired
	private ChatUserRepository chatUserRepo;

	@Autowired
	private StudentRepository studentRepo;

	@Autowired
	private TeacherRepository teacherRepo;

	@Autowired
	private TeacherProfileRepo teacherProfileRepo;

	@Autowired
	private CourseRepository courseRepo;

	@Autowired
	private EnrollmentRepository enrollmentRepo;

	@Autowired
	private ConnectionRepository connectionRepo;

	@Autowired
	private InstructorFeedbackRepository feedbackRepo;

	@GetMapping("/student-view-profile")
	public String viewProfile(@RequestParam Integer userId) {

		ChatUser user = chatUserRepo.findById(userId).orElse(null);

		if (user == null) {
			return "redirect:/student-chat";
		}

		if (user.getType() == UserType.STUDENT) {
			return "redirect:/student-profile-view?id=" + user.getRefId();
		}

		else if (user.getType() == UserType.TEACHER) {
			return "redirect:/student-teacher-view?id=" + user.getRefId();
		}

		return "redirect:/student-chat";
	}

	// student-profile view i mean student to student view method is in connection controller

	@GetMapping("/student-teacher-view")
	public String viewTeacherDashboard(@RequestParam Integer id, HttpSession session, Model model) {

		Integer myId = (Integer) session.getAttribute("studentId");
		if (myId == null)
			return "redirect:/student-login";

		Teacher teacher = teacherRepo.findById(id).orElse(null);

		if (teacher == null) {
			return "redirect:/student-chat";
		}

		TeacherProfile profile = teacherProfileRepo.findByTeacherTeacherId(id);

		List<InstructorFeedback> feedbacks = feedbackRepo.findByTeacherTeacherId(id);

		double avgRating = feedbackRepo.getAverageRating(id);
		Long totalRatings = feedbackRepo.getTotalRatings(id);

		if (avgRating == 0)
			avgRating = 0.0;
		if (totalRatings == null)
			totalRatings = 0L;

		List<Course> courses = courseRepo.findByTeacherTeacherIdAndStatusNot(id, CourseStatus.DELETED);

		long totalCourses = courses.size();

		Map<Integer, Boolean> enrolledMap = new HashMap<>();

		for (Course c : courses) {

			boolean enrolled = enrollmentRepo.existsByStudentStudidAndCourseCourseId(myId, c.getCourseId());

			enrolledMap.put(c.getCourseId(), enrolled);
		}

		ChatUser chatUser = chatUserRepo.findByRefIdAndType(id, UserType.TEACHER).orElse(null);

		boolean canRateInstructor = enrollmentRepo.findByStudentStudid(myId).stream()
				.filter(e -> e.getCompletedAt() != null)
				.anyMatch(e -> e.getCourse().getTeacher().getTeacherId().equals(id));

		boolean instructorFeedbackGiven = feedbackRepo.existsByTeacherTeacherIdAndStudentStudid(id, myId);

		boolean alreadyEnrolled = enrollmentRepo.findByStudentStudid(myId).stream()
				.anyMatch(e -> e.getCourse().getTeacher().getTeacherId().equals(id));

		model.addAttribute("canRateInstructor", canRateInstructor);
		model.addAttribute("instructorFeedbackGiven", instructorFeedbackGiven);
		model.addAttribute("alreadyEnrolled", alreadyEnrolled);

		model.addAttribute("chatUser", chatUser);
		model.addAttribute("teacher", teacher);
		model.addAttribute("profile", profile);
		model.addAttribute("feedbacks", feedbacks != null ? feedbacks : List.of());
		model.addAttribute("avgRating", avgRating);
		model.addAttribute("totalRatings", totalRatings);
		model.addAttribute("courses", courses);
		model.addAttribute("totalCourses", totalCourses);
		model.addAttribute("enrolledMap", enrolledMap);

		return "student-teacher-view";
	}
}