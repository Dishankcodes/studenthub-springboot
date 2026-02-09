package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.entity.Course;
import com.example.demo.entity.Teacher;
import com.example.demo.enums.CourseStatus;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.TeacherRepository;

@Controller
public class TeacherCourseController {

	@Autowired
	private TeacherRepository teacherRepo;
	@Autowired
	private CourseRepository courseRepo;

	// ===== COURSE MANAGEMENT =====
	@GetMapping("/teacher-course")
	public String courseManagement(Model model) {

		Integer teacherId = 1;

		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);

		model.addAttribute("teacher", teacher);
		return "teacher-courses";
	}

	@GetMapping("/teacher-creates-course")
	public String createCourse(Model model) {

		model.addAttribute("course", new Course());
		return "teacher-creates-course";
	}

	@PostMapping("/teacher-creates-course")
	public String saveCourse(@ModelAttribute Course course) {

		Integer teacherId = 1;
		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
		
		course.setTeacher(teacher);
		
		course.setStatus(CourseStatus.DRAFT);
		
		courseRepo.save(course);
		return "redirect:/teacher-course";

	}
}
