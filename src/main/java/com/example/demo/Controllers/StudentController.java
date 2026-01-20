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
	
	
}
