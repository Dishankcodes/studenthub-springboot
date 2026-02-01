package com.example.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.Student;
import com.example.demo.repository.StudentRepo;

@Controller
public class AdminController {
	
	@Autowired
	private StudentRepo repo;

	@GetMapping("/admin-dashboard")
	public String admin_dashboard()
	{
	
		return "admin-dashboard";
	}
	
	@GetMapping("/manage-students")
	public String admin_student(Model model)
	{
	
		List<Student> students=repo.findAll();
		model.addAttribute("students", students);
		return "manage-students";
	}
	
	@GetMapping("/manage-teachers")
	public String admin_instructor()
	{
	
		return "manage-instructor";
	}
	
	@GetMapping("/manage-courses")
	public String admin_courses()
	{
	
		return "manage-courses";
	}
	
	@GetMapping("/manage-internships")
	public String admin_internships()
	{
	
		return "manage-internships";
	}
	@GetMapping("/admin-feedback")
	public String admin_feedback()
	{
	
		return "admin-feedback";
	}
	
	@GetMapping("/admin-settings")
	public String admin_settings()
	{
	
		return "admin-settings";
	}
}
