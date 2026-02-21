package com.example.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.dto.AdminCourseSummaryDTO;
import com.example.demo.entity.Admin;
import com.example.demo.entity.Course;
import com.example.demo.enums.CourseStatus;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.CourseRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminManageCourseController {

	@Autowired
	private AdminRepository adminRepo;

	@Autowired
	private CourseRepository courseRepo;

	@GetMapping("/manage-courses")
	public String admin_courses(HttpSession session, Model model) {

		Integer adminId = 1;

		Admin admin = adminRepo.findById(adminId).orElse(null);

//		
//		if (session.getAttribute("adminEmail") == null) {
//			return "redirect:/admin-login";
//		}

		List<AdminCourseSummaryDTO> courses = courseRepo.fetchAdminCourseSummary();

		model.addAttribute("courses", courses);
		model.addAttribute("username", admin.getUsername());
		// model.addAttribute("username", session.getAttribute("adminUsername"));
		return "manage-courses";
	}

	@PostMapping("/manage-course/status/{id}")
	public String updateCourseStatus(@PathVariable Integer id, @RequestParam CourseStatus status) {

		Course course = courseRepo.findById(id).orElse(null);
		course.setStatus(status);
		courseRepo.save(course);

		return "redirect:/manage-courses#course-" + id;
	}

	@PostMapping("/manage-course/delete/{id}")
	public String deleteCourse(@PathVariable Integer id, HttpSession session) {

		if (session.getAttribute("adminEmail") == null) {
			return "redirect:/admin-login";
		}

		Course course = courseRepo.findById(id).orElseThrow();
		course.setStatus(CourseStatus.DELETED);
		courseRepo.save(course);

		return "redirect:/manage-courses";
	}

	@GetMapping("/admin-course/view/{id}")
	public String viewCourse(@PathVariable Integer id, Model model) {
		
		
		Course course = courseRepo.findAllWithStructure(id);
		
		if (course == null) {
			return "redirect:/manage-courses";
		}

		model.addAttribute("course", course);
		return "admin-course-view";
	}
}
