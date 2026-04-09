package com.example.demo.Controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.Teacher;
import com.example.demo.enums.CourseStatus;
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

	    Integer teacherId = (Integer) session.getAttribute("teacherId");
	    if (teacherId == null) return "redirect:/teacher-auth";

	    Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
	    if (teacher == null) {
	        session.invalidate();
	        return "redirect:/teacher-auth";
	    }

	    model.addAttribute("teacher", teacher);

	    // ================= 🌍 PLATFORM =================

	    long platformFreeCourses = courseRepo
	    	    .countByPriceLessThanEqualAndStatus(0.0, CourseStatus.PUBLISHED);

	    long platformPaidCourses = courseRepo
	    	    .countByPriceGreaterThanAndStatus(0.0, CourseStatus.PUBLISHED);

	    long platformCourses = platformFreeCourses + platformPaidCourses;

	    long platformStudents = enrollmentRepo.countDistinctAllStudents();

	    model.addAttribute("platformFreeCourses", platformFreeCourses);
	    model.addAttribute("platformPaidCourses", platformPaidCourses);
	    model.addAttribute("platformCourses", platformCourses);
	    model.addAttribute("platformStudents", platformStudents);

	    // ================= 👨‍🏫 TEACHER =================

	    long teacherFreeCourses = courseRepo
	    	    .countByTeacherTeacherIdAndPriceLessThanEqualAndStatus(
	    	        teacherId, 0.0, CourseStatus.PUBLISHED);

	    	long teacherPaidCourses = courseRepo
	    	    .countByTeacherTeacherIdAndPriceGreaterThanAndStatus(
	    	        teacherId, 0.0, CourseStatus.PUBLISHED);
	    long teacherCourses = teacherFreeCourses + teacherPaidCourses;

	    long teacherStudents = enrollmentRepo.countDistinctStudentsByTeacher(teacherId);

	    Double totalRevenue = enrollmentRepo.getTotalRevenueByTeacher(teacherId);
	    if (totalRevenue == null) totalRevenue = 0.0;

	    model.addAttribute("teacherFreeCourses", teacherFreeCourses);
	    model.addAttribute("teacherPaidCourses", teacherPaidCourses);
	    model.addAttribute("teacherCourses", teacherCourses);
	    model.addAttribute("teacherStudents", teacherStudents);
	    model.addAttribute("totalRevenue", totalRevenue.longValue());

	    // ================= TOP COURSE =================

	    List<Object[]> topCourses = courseRepo.findTopCoursesByEnrollment(teacherId);

	    if (topCourses != null && !topCourses.isEmpty()) {
	   
	        Object[] row = topCourses.get(0);

	        model.addAttribute("topCourseName", row[0]);
	        model.addAttribute("topCourseStudents", row[1]);
	        model.addAttribute("topCourseRating", row[2] != null ? row[2] : 0);
	        model.addAttribute("topCourseRevenue", row[3] != null ? row[3] : 0);

	    } else {
	        model.addAttribute("topCourseName", "N/A");
	        model.addAttribute("topCourseStudents", 0);
	        model.addAttribute("topCourseRating", 0);
	        model.addAttribute("topCourseRevenue", 0);
	    }

	    // ================= OTHER =================

	    List<Object[]> rawData = enrollmentRepo.countStudentsGroupedByMonth(teacherId);

	    List<Map<String, Object>> monthlyStudents = new ArrayList<>();

	    for (Object[] row : rawData) {
	        Map<String, Object> map = new HashMap<>();
	        map.put("month", row[0]);
	        map.put("count", row[1]);
	        monthlyStudents.add(map);
	    }

	    model.addAttribute("monthlyStudents", monthlyStudents);

	    model.addAttribute("courseIncomeList",
	            courseRepo.getCourseIncomeStats(teacherId));

	    model.addAttribute("feedbackList",
	            instructorFeedbackRepo.findTop5ByTeacherTeacherIdOrderByCreatedAtDesc(teacherId));

	    model.addAttribute("teacherStatus", teacher.getStatus());

	    return "teacher-dashboard";
	}
}