package com.example.demo.Controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.Announcement;
import com.example.demo.entity.NoteCategory;
import com.example.demo.entity.TeacherNotes;
import com.example.demo.enums.AnnouncementAudience;
import com.example.demo.enums.AnnouncementType;
import com.example.demo.enums.NoteStatus;
import com.example.demo.repository.AnnouncementRepository;
import com.example.demo.repository.InstructorFeedbackRepository;
import com.example.demo.repository.NoteCategoryRepository;
import com.example.demo.repository.TeacherNotesRepository;

@Controller
public class AdminOtherController {

	@Autowired
	private NoteCategoryRepository categoryRepo;

	@Autowired
	private TeacherNotesRepository teacherNoteRepo;

	@Autowired
	private InstructorFeedbackRepository instructorFeedbackRepo;

	@Autowired
	private AnnouncementRepository announcementRepo;

	@GetMapping("/admin-note-categories")
	public String noteCategories(Model model) {

		model.addAttribute("categories", categoryRepo.findAll());
		model.addAttribute("pendingNotes", teacherNoteRepo.findByStatus(NoteStatus.PENDING));
		model.addAttribute("allNotes", teacherNoteRepo.findAll());

		return "admin-note-categories";
	}

	@PostMapping("/admin-note-categories")
	public String addCategory(@RequestParam String name, RedirectAttributes ra) {
		String trimmedName = name.trim();

		if (categoryRepo.existsByNameIgnoreCase(trimmedName)) {
			ra.addFlashAttribute("error", "⚠️ Category '" + trimmedName + "' already exists");
			return "redirect:/admin-note-categories";
		}

		NoteCategory c = new NoteCategory();
		c.setName(trimmedName);
		c.setActive(true);

		categoryRepo.save(c);

		ra.addFlashAttribute("success", "✅ Category '" + trimmedName + "' added successfully");

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
	public String approveNote(@PathVariable Integer id, RedirectAttributes ra) {

		TeacherNotes note = teacherNoteRepo.findById(id).orElseThrow();
		note.setStatus(NoteStatus.APPROVED);
		note.setApproved(true);
		note.setApprovedAt(LocalDateTime.now());

		teacherNoteRepo.save(note);
		ra.addFlashAttribute("teacherMsg", "approved:" + note.getTitle());
		return "redirect:/admin-note-categories";
	}

	@PostMapping("/admin/notes/reject/{id}")
	public String rejectNote(@PathVariable Integer id, RedirectAttributes ra) {

		TeacherNotes note = teacherNoteRepo.findById(id).orElseThrow();
		note.setStatus(NoteStatus.REJECTED);
		note.setApproved(false);

		teacherNoteRepo.save(note);
		ra.addFlashAttribute("teacherMsg", "rejected:" + note.getTitle());
		return "redirect:/admin-note-categories";
	}

	@GetMapping("/admin-announcement")
	public String adminAnnouncemnt(Model model) {

		model.addAttribute("announcements", announcementRepo.findAll());
		return "admin-announcement";
	}

	@PostMapping("/admin-announcement/create")
	public String createAdminAnnouncement(@RequestParam String title, @RequestParam String message,
			@RequestParam AnnouncementType type, @RequestParam AnnouncementAudience audience,
			@RequestParam(required = false) MultipartFile file) throws IOException {

		Announcement a = new Announcement();
		a.setTitle(title);
		a.setMessage(message);
		a.setType(type);
		a.setAudience(audience);
		a.setTeacher(null); // Admin

		if (file != null && !file.isEmpty()) {

			String dir = System.getProperty("user.dir") + "/uploads/announcements/";
			Files.createDirectories(Paths.get(dir));

			String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
			file.transferTo(new File(dir + fileName));

			a.setAttachmentUrl("/uploads/announcements/" + fileName);
			a.setAttachmentName(file.getOriginalFilename());
		}

		announcementRepo.save(a);

		return "redirect:/admin-announcement?created";
	}

	@PostMapping("/admin-announcement/update")
	public String updateAnnouncement(@RequestParam Integer id, @RequestParam String title, @RequestParam String message,
			@RequestParam AnnouncementAudience audience) {

		Announcement a = announcementRepo.findById(id).orElseThrow();

		a.setTitle(title);
		a.setMessage(message);
		a.setAudience(audience);

		announcementRepo.save(a);

		return "redirect:/admin-announcement?updated";
	}

	@PostMapping("/admin-announcement/delete/{id}")
	public String deleteAnnouncement(@PathVariable Integer id) {

		Announcement a = announcementRepo.findById(id).orElseThrow();
		a.setActive(false);

		announcementRepo.save(a);

		return "redirect:/admin-announcement?deleted";
	}

	@PostMapping("/admin-announcement/pin/{id}")
	public String togglePin(@PathVariable Integer id) {

		Announcement a = announcementRepo.findById(id).orElseThrow();
		a.setPinned(!a.isPinned());
		announcementRepo.save(a);

		return "redirect:/admin-announcement";
	}
}
