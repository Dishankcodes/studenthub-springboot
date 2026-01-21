package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

	@GetMapping("/admin-dashboard")
	public String admin_dashboard()
	{
	
		return "admin-dashboard";
	}
	
	@GetMapping("/manage-students")
	public String admin_student()
	{
	
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
