package com.example.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.InstructorFeedback;
import com.example.demo.entity.Student;
import com.example.demo.entity.Teacher;
import com.example.demo.entity.TeacherProfile;
import com.example.demo.enums.TeacherStatus;
import com.example.demo.repository.AnnouncementRepository;
import com.example.demo.repository.InstructorFeedbackRepository;
import com.example.demo.repository.NoteCategoryRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TeacherNotesRepository;
import com.example.demo.repository.TeacherProfileRepo;
import com.example.demo.repository.TeacherRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

	@Autowired
	private StudentRepository repo;

	@Autowired
	private TeacherRepository teacherRepo;

	@Autowired
	private TeacherProfileRepo teacherProfileRepo;

	@Autowired
	private NoteCategoryRepository categoryRepo;

	@Autowired
	private TeacherNotesRepository teacherNoteRepo;

	@Autowired
	private InstructorFeedbackRepository instructorFeedbackRepo;



	@GetMapping("/admin-dashboard")
	public String admin_dashboard(HttpSession session, Model model) {
		if (session.getAttribute("adminEmail") == null) {
			return "redirect:/admin-login";
		}

		String username = (String) session.getAttribute("adminUsername");
		model.addAttribute("username", username);
		return "admin-dashboard";
	}

	@GetMapping("/manage-students")
	public String admin_student(HttpSession session, Model model) {

		if (session.getAttribute("adminEmail") == null) {
			return "redirect:/admin-login";
		}
		List<Student> students = repo.findAll();
		model.addAttribute("students", students);

		String username = (String) session.getAttribute("adminUsername");
		model.addAttribute("username", username);

		return "manage-students";
	}

	@PostMapping("/admin-student/delete/{studId}")
	public String deleteStudent(@PathVariable Integer studId, RedirectAttributes ra, HttpSession session) {

		if (session.getAttribute("adminEmail") == null) {
			return "redirect:/admin-login";
		}

		repo.deleteById(studId); // 🔥 cascades automatically

		ra.addFlashAttribute("success", "✅ Student deleted permanently");

		return "redirect:/manage-students";
	}

	@GetMapping("/manage-teachers")
	public String admin_instructor(HttpSession session, Model model) {

		if (session.getAttribute("adminEmail") == null) {
			return "redirect:/admin-login";
		}

		List<Teacher> teachers = teacherRepo.findAll();
		model.addAttribute("teachers", teachers);

		String username = (String) session.getAttribute("adminUsername");
		model.addAttribute("username", username);

		return "manage-instructor";
	}

	@GetMapping("/admin-instructor-view/{teacherId}")
	public String viewInstructorAndFeedback(@PathVariable Integer teacherId, Model model, HttpSession session) {

		if (session.getAttribute("adminEmail") == null) {
			return "redirect:/admin-login";
		}

		Teacher teacher = teacherRepo.findById(teacherId).orElseThrow();
		TeacherProfile profile = teacherProfileRepo.findByTeacherTeacherId(teacherId);

		List<InstructorFeedback> feedbacks = instructorFeedbackRepo.findByTeacherTeacherId(teacherId);

		double avgRating = instructorFeedbackRepo.getAverageRating(teacherId);

		Long totalRatings = instructorFeedbackRepo.getTotalRatings(teacherId);

		model.addAttribute("teacher", teacher);
		model.addAttribute("profile", profile);
		model.addAttribute("feedbacks", feedbacks);
		model.addAttribute("avgRating", avgRating);
		model.addAttribute("totalRatings", totalRatings);
		model.addAttribute("username", session.getAttribute("adminUsername"));

		return "admin-instructor-view";
	}

	
	@PostMapping("/manage-instructor/status/{id}")
	public String updateTeacherStatus(@PathVariable Integer id,
			@RequestParam TeacherStatus status)
	{
		 Teacher teacher = teacherRepo.findById(id).orElse(null);

		    if (teacher == null) {
		        return "redirect:/manage-instructor";
		    }

		    teacher.setStatus(status);
		    teacherRepo.save(teacher);

		    return "redirect:/manage-teachers#instructor-" + id;
	}
	

	@GetMapping("/manage-internships")
	public String admin_internships() {

		return "manage-internships";
	}

	@GetMapping("/admin-feedback")
	public String admin_feedback() {

		return "admin-feedback";
	}

	@GetMapping("/admin-settings")
	public String admin_settings() {

		return "admin-settings";
	}

	
	@GetMapping("/admin-logout")
	public String adminLogout(HttpSession session) {
		session.invalidate();
		return "redirect:/admin-login";
	}
}
