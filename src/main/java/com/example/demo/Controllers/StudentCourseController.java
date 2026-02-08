package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentCourseController {

	@GetMapping("/student-course")
	public String exploreCourse() {

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
