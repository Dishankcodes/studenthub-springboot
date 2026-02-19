package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class TeacherUploadController {

	
	
	public String uploadVideo(@RequestParam Integer lessonId,
			@RequestParam("video") MultipartFile video,
			@RequestParam(required = false) Boolean freePreview) {
		
		
		return "redirect:/teacher-creates-course?courseId="
				;
	}
}
