package com.example.demo.Controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.ChatUser;
import com.example.demo.entity.Enrollment;
import com.example.demo.entity.Teacher;
import com.example.demo.enums.UserType;
import com.example.demo.repository.ChatUserRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.TeacherRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class TeacherChatController {

	@Autowired
	private ChatUserRepository chatUserRepo;
	
	@Autowired
	private EnrollmentRepository enrollmentRepo;

	@Autowired
	private TeacherRepository teacherRepo;
	
	
	@GetMapping("/teacher-chat")
	public String teacherChat(HttpSession session, Model model) {

	
		Integer teacherId = (Integer) session.getAttribute("teacherId");
		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
		if (teacher == null) {
			session.invalidate();
			return "redirect:/teacher-auth";
		}

		ChatUser me = chatUserRepo.findByRefIdAndType(teacherId, UserType.TEACHER).orElseThrow();

		List<ChatUser> students = new ArrayList<>();

		List<Enrollment> enrollments = enrollmentRepo.findByTeacherId(teacherId);

		for (Enrollment e : enrollments) {
			chatUserRepo.findByRefIdAndType(e.getStudent().getStudid(), UserType.STUDENT).ifPresent(students::add);
		}

		model.addAttribute("users", students);
		model.addAttribute("me", me);
		model.addAttribute("teacher", teacher);

		return "teacher-chat";
	}
}