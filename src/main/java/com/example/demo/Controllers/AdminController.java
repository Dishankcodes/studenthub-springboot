package com.example.demo.Controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.NoteCategory;
import com.example.demo.entity.Student;
import com.example.demo.entity.Teacher;
import com.example.demo.entity.TeacherNotes;
import com.example.demo.enums.NoteStatus;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.NoteCategoryRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TeacherNotesRepository;
import com.example.demo.repository.TeacherRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

	@Autowired
	private StudentRepository repo;

	@Autowired
	TeacherRepository teacherRepo;
	
	@Autowired
	CourseRepository courseRepo;
	
	@Autowired
	private AdminRepository adminRepo;
	
	@Autowired
	private NoteCategoryRepository categoryRepo;
	
	@Autowired
	private TeacherNotesRepository teacherNoteRepo;


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
	public String deleteStudent(
	        @PathVariable Integer studId,
	        RedirectAttributes ra,
	        HttpSession session) {

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

	
	@PostMapping("/admin-teacher/delete/{teacherId}")
	public String deleteTeacher(
	        @PathVariable Integer teacherId,
	        RedirectAttributes ra,
	        HttpSession session
	) {
	    if (session.getAttribute("adminEmail") == null) {
	        return "redirect:/admin-login";
	    }

	    teacherRepo.deleteById(teacherId);

	    ra.addFlashAttribute("success", "✅ Instructor deleted successfully");

	    return "redirect:/manage-teachers";
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
	
	@GetMapping("/admin-note-categories")
	public String noteCategories(Model model) {

	    model.addAttribute("categories", categoryRepo.findAll());
	    model.addAttribute("pendingNotes",
	        teacherNoteRepo.findByStatus(NoteStatus.PENDING));

	    return "admin-note-categories";
	}

	@PostMapping("/admin-note-categories")
	public String addCategory(@RequestParam String name) {
	    NoteCategory c = new NoteCategory();
	    c.setName(name);
	    categoryRepo.save(c);
	    return "redirect:/admin-note-categories";
	}

	@PostMapping("/admin-note-categories/toggle/{id}")
	public String toggleCategory(@PathVariable Integer id) {
	    NoteCategory c = categoryRepo.findById(id).orElseThrow();
	    c.setActive(!c.isActive());
	    categoryRepo.save(c);
	    return "redirect:/admin-note-categories";
	}
	
	@PostMapping("/admin/notes/approve/{id}")
	public String approveNote(@PathVariable Integer id) {

	    TeacherNotes note = teacherNoteRepo.findById(id).orElseThrow();
	    note.setStatus(NoteStatus.APPROVED);
	    note.setApproved(true);
	    note.setApprovedAt(LocalDateTime.now());

	    teacherNoteRepo.save(note);
	    return "redirect:/admin-note-categories";
	}
	@PostMapping("/admin/notes/reject/{id}")
	public String rejectNote(@PathVariable Integer id) {

	    TeacherNotes note = teacherNoteRepo.findById(id).orElseThrow();
	    note.setStatus(NoteStatus.REJECTED);
	    note.setApproved(false);

	    teacherNoteRepo.save(note);
	    return "redirect:/admin-note-categories";
	}

	@GetMapping("/admin-logout")
	public String adminLogout(HttpSession session) {
		session.invalidate();
		return "redirect:/admin-login";
	}

}
