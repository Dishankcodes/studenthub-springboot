package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.Teacher;
import com.example.demo.repository.TeacherRepository;

@Controller
public class TeacherCourseController {

	@Autowired
	private TeacherRepository teacherRepo;

	// ===== COURSE MANAGEMENT =====
	@GetMapping("/teacher-course")
	public String courseManagement(Model model) {

		Integer teacherId = 1;

		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);

		model.addAttribute("teacher", teacher);
		return "teacher-courses";
	}

	@GetMapping("/teacher-creates-course")
	public String createCourse() {
		return "teacher-creates-course";
	}
}
