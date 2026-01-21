package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentController {

	
	@GetMapping("/student-dashboard")
	public String student_dashboard()
	{
	
		return "student-dashboard";
	}
	
	@GetMapping("/student-course")
	public String student_course()
	{
	
		return "student-course";
	}
	
	@GetMapping("/student-learning")
	public String student_learning()
	{
	
		return "student-learning";
	}
	
	@GetMapping("/student-news")
	public String student_announcements() {
		return "student-announcements";
	}
	
	@GetMapping("/student-notes")
	public String student_stuff() {
		return "student-stuff";
	}
	
	@GetMapping("/student-chat")
	public String student_chat() {
		return "student-chat";
	}
	
	@GetMapping("/student-internships")
	public String student_internships()
	{
		return "student-internships";
	}
	
	@GetMapping("/student-notification")
	public String student_notifi()
	{
		return "student-notification";
	}
	
	@GetMapping("/student-profile")
	public String student_profile()
	{
		return "student-profile";
	}
	
	@GetMapping("/student-instructor-feedback")
	public String student_instructor_feedback()
	{
		return "student_inst_review";
	}
	
}
