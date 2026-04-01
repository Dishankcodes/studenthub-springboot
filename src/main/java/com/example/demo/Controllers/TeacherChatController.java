package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.Teacher;
import com.example.demo.repository.TeacherRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class TeacherChatController {

	
	@Autowired
	private TeacherRepository teacherRepo;
	
	
	@GetMapping("/teacher-chat")
	public String communicationPage(HttpSession session, Model model) {

		Integer teacherId = 1;
		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
		if (teacher == null) {
			session.invalidate();
			return "redirect:/teacher-auth";
		}

		model.addAttribute("teacher", teacher);
		return "teacher-chat";
	}
}
