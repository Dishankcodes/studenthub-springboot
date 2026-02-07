package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TeacherCourseController {

	 // ===== COURSE MANAGEMENT =====
    @GetMapping("/teacher-course")
    public String courseManagement() {
        return "teacher-courses";
    }

    @GetMapping("/teacher-creates-course")
    public String createCourse()
    {
    	return "teacher-creates-course";
    }
}
