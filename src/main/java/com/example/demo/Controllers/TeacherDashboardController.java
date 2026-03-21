package com.example.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.demo.entity.Course;
import com.example.demo.entity.InstructorFeedback;
import com.example.demo.entity.Teacher;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.InstructorFeedbackRepository;
import com.example.demo.repository.TeacherRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class TeacherDashboardController {

	@Autowired
	private TeacherRepository teacherRepo;

	@Autowired
	private EnrollmentRepository enrollmentRepo;

	@Autowired
	private CourseRepository courseRepo;

	@Autowired
	private InstructorFeedbackRepository instructorFeedbackRepo;

	@GetMapping("/teacher-dashboard")
	public String teacherDashboard(Model model, HttpSession session) {

		// Boolean loggedIn = (Boolean) session.getAttribute("TEACHER_LOGGED_IN");
		// Integer teacherId = (Integer) session.getAttribute("teacherId");

		Integer teacherId = 1; // 🔥 REMOVE after testing

		// if (loggedIn == null || !loggedIn || teacherId == null) {
		// return "redirect:/teacher-auth";
		// }

		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
		if (teacher == null) {
			session.invalidate();
			return "redirect:/teacher-auth";
		}

		model.addAttribute("teacher", teacher);

		long freeCourses = courseRepo.countByTeacherTeacherIdAndPriceIsNull(teacherId);

		long paidCourses = courseRepo.countByTeacherTeacherIdAndPriceGreaterThan(teacherId, 0.0);

		long totalCourses = freeCourses + paidCourses;

		long totalStudents = enrollmentRepo.countDistinctStudentsByTeacher(teacherId);

		model.addAttribute("freeCourses", freeCourses);
		model.addAttribute("paidCourses", paidCourses);
		model.addAttribute("totalCourses", totalCourses);
		model.addAttribute("totalStudents", totalStudents);
		model.addAttribute("teacherCourses", totalCourses);
		model.addAttribute("teacherStudents", totalStudents);

		Double totalRevenue = enrollmentRepo.getTotalRevenueByTeacher(teacherId);

		if (totalRevenue == null) {
			totalRevenue = 0.0;
		}

		model.addAttribute("totalRevenue", totalRevenue.longValue());

		List<Object[]> topCourses = courseRepo.findTopCoursesByEnrollment(teacherId);

		if (topCourses != null && !topCourses.isEmpty()) {

			Object[] row = topCourses.get(0);

			model.addAttribute("name", row[0]); // course title
			model.addAttribute("studentCount", row[1]); // enrollment count
			model.addAttribute("rating", row[2] != null ? row[2] : 0);

		} else {

			model.addAttribute("name", "N/A");
			model.addAttribute("studentCount", 0);
			model.addAttribute("rating", 0);
		}

		List<Object[]> courseIncomeList = courseRepo.getCourseIncomeStats(teacherId);

		List<InstructorFeedback> feedbackList = instructorFeedbackRepo
				.findTop5ByTeacherTeacherIdOrderByCreatedAtDesc(teacherId);

		List<Object[]> monthlyStudents = enrollmentRepo.countStudentsGroupedByMonth(teacherId);

		model.addAttribute("courseIncomeList", courseIncomeList);
		model.addAttribute("feedbackList", feedbackList);
		model.addAttribute("teacherStatus", teacher.getStatus());
		model.addAttribute("monthlyStudents", monthlyStudents);

		return "teacher-dashboard";
	}

}