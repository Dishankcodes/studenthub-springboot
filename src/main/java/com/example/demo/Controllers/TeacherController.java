package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.entity.Teacher;
import com.example.demo.entity.TeacherProfile;
import com.example.demo.repository.TeacherProfileRepo;
import com.example.demo.repository.TeacherRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class TeacherController {

	@Autowired
	private TeacherRepository teacherRepo;

	@Autowired
	private TeacherProfileRepo teacherProfileRepo;

	// ===== DASHBOARD =====
	@GetMapping("/teacher-dashboard")
	public String dashboard(Model model)
	// HttpSession session)
	{
//	    if (session.getAttribute("TEACHER_LOGGED_IN") == null) {
//	        return "redirect:/teacher-auth";
//	    }//	  

		Integer teacherId = 1;

		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);

		if (teacher == null) {
			return "redirect:/teacher-auth";
		}
		model.addAttribute("teacher", teacher);
		return "teacher-dashboard";
	}

	// ===== EXAMS & QUIZZES =====
	@GetMapping("/teacher-exams")
	public String examsAndQuizzes() {
		return "teacher-exams";
	}

	// ===== STUDENTS =====
	@GetMapping("/teacher-students")
	public String studentsPage() {
		return "teacher-students";
	}

	// ===== COMMUNICATION =====
	@GetMapping("/teacher-communication")
	public String communicationPage() {
		return "teacher-communication";
	}

	// ===== FEEDBACK =====
	@GetMapping("/teacher-feedback")
	public String feedbackPage() {
		return "teacher-feedback";
	}

	// ===== PROFILE =====
	@GetMapping("/teacher-profile")
	public String teacherProfile(
			// HttpSession session,
			Model model) {

		// Boolean loggedIn = (Boolean) session.getAttribute("TEACHER_LOGGED_IN");
		// Integer teacherId = (Integer) session.getAttribute("teacherId");

		Integer teacherId = 1; // remove this when testing done

		// if (loggedIn == null || !loggedIn || teacherId == null) {
		// return "redirect:/teacher-auth";
		// }

		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
		if (teacher == null) {
			// session.invalidate();
			return "redirect:/teacher-auth";
		}

		TeacherProfile profile = teacherProfileRepo.findByTeacherTeacherId(teacherId);

		if (profile == null) {
			profile = new TeacherProfile();
			profile.setTeacher(teacher);
		}

		model.addAttribute("teacher", teacher);
		model.addAttribute("profile", profile);

		return "teacher-profile";
	}

	@PostMapping("/teacher-profile")
	public String editTeacherProfile(@ModelAttribute("profile") TeacherProfile teacherProfile
	// HttpSession session
	) {

		Integer teacherId = 1;// demo
		// Boolean loggedIn = (Boolean) session.getAttribute("TEACHER_LOGGED_IN");
		// Integer teacherId = (Integer) session.getAttribute("teacherId");

		// if (loggedIn == null || !loggedIn || teacherId == null) {
		// return "redirect:/teacher-auth";
		// }

		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
		if (teacher == null) {
			// session.invalidate();
			return "redirect:/teacher-auth";
		}

		TeacherProfile profile = teacherProfileRepo.findByTeacherTeacherId(teacherId);

		if (profile == null) {
			profile = new TeacherProfile();
			profile.setTeacher(teacher);
		}

		profile.setQualification(teacherProfile.getQualification());
		profile.setSpecialist(teacherProfile.getSpecialist());
		profile.setExperience(teacherProfile.getExperience());
		profile.setBio(teacherProfile.getBio());

		teacherProfileRepo.save(profile);

		return "redirect:/teacher-profile";
	}

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/teacher-auth";
	}

}
