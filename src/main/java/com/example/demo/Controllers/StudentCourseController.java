package com.example.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.Course;
import com.example.demo.enums.CourseStatus;
import com.example.demo.repository.CourseRepository;

@Controller
public class StudentCourseController {

	@Autowired
	private CourseRepository courseRepo;
	
	@GetMapping("/student-course")
	public String exploreCourse(Model model) {

		List<Course> courses = courseRepo.findByStatus(CourseStatus.PUBLISHED);
		
		model.addAttribute("courses", courses);
		return "student-course";
	}

	@GetMapping("/student-course-details")
	public String viewCourse() {
		return "student-course-details";
	}

	@GetMapping("/student-enroll")
	public String enrollCourse() {
		return "student-enroll";
	}
}
