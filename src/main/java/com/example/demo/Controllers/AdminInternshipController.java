package com.example.demo.Controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.Service.EmailService;
import com.example.demo.entity.Admin;
import com.example.demo.entity.Course;
import com.example.demo.entity.InternshipApplication;
import com.example.demo.entity.InternshipTest;
import com.example.demo.entity.Internships;
import com.example.demo.entity.Student;
import com.example.demo.enums.ApplicationStatus;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.InternshipRepository;
import com.example.demo.repository.InternshipTestRepository;
import com.example.demo.repository.StudentRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminInternshipController {

	@Autowired
	private InternshipRepository internshipRepo;

	@Autowired
	private CourseRepository courseRepo;

	@Autowired
	private AdminRepository adminRepo;

	@Autowired
	private ApplicationRepository applicationRepo;

	@Autowired
	private StudentRepository studentRepo;

	@Autowired
	private EmailService emailService;

	@Autowired
	private InternshipTestRepository testRepo;

	@GetMapping("/manage-internships")
	public String manageInternships(Model model, HttpSession session) {

		if (session.getAttribute("adminEmail") == null) {
			return "redirect:/admin-login";
		}

		String username = (String) session.getAttribute("adminUsername");

		List<Internships> internships = internshipRepo.findAll();

		model.addAttribute("username", username);
		model.addAttribute("internships", internships);

		return "manage-internships";
	}

	@GetMapping("/admin-post-internships")
	public String postInternshipPage(Model model) {

		List<Course> courses = courseRepo.findAll();
		model.addAttribute("courses", courses);

		return "admin-post-internships";
	}

	@PostMapping("/admin/internship/save")
	public String saveInternship(@RequestParam String title, @RequestParam String role, @RequestParam String type,
			@RequestParam String location, @RequestParam String skills, @RequestParam(required = false) Integer stipend,
			@RequestParam String duration, @RequestParam(required = false) String startDate,
			@RequestParam String description, @RequestParam(required = false) Integer courseId,
			@RequestParam Boolean hasTest) {

		Integer adminId = 1;
		Admin admin = adminRepo.findById(adminId).orElse(null);

		Internships i = new Internships();

		i.setTitle(title);
		i.setRole(role);
		i.setType(type);
		i.setLocation(location);
		i.setSkills(skills);
		i.setStipend(stipend);
		i.setDuration(duration);
		i.setHasTest(hasTest);
		if (startDate != null && !startDate.isEmpty()) {
			i.setStartDate(LocalDate.parse(startDate));
		}
		i.setDescription(description);
		i.setAdmin(admin);

		if (courseId != null) {
			Course course = courseRepo.findById(courseId).orElse(null);
			i.setRequiredCourse(course);
		}

		internshipRepo.save(i);

		return "redirect:/manage-internships";
	}

	@GetMapping("/admin-applicants")
	public String viewApplicants(@RequestParam Integer id, Model model) {

		Internships internship = internshipRepo.findById(id).orElse(null);

		List<InternshipApplication> applications = applicationRepo.findByInternshipId(id);

		model.addAttribute("internship", internship);
		model.addAttribute("applications", applications);

		return "manage-applicants";
	}

	@GetMapping("/admin-view-student")
	public String viewStudent(@RequestParam Integer id, @RequestParam Integer internshipId, Model model) {

		InternshipApplication app = applicationRepo.findByStudent_StudidAndInternship_Id(id, internshipId).orElse(null);

		if (app == null) {
			model.addAttribute("error", "Application not found");
			return "admin-student-profile";
		}

		model.addAttribute("app", app);
		model.addAttribute("student", app.getStudent());
		model.addAttribute("internship", app.getInternship());

		return "admin-student-profile";
	}

	@PostMapping("/admin/application/update")
	public String updateApplication(@RequestParam Integer appId, @RequestParam String action,
			@RequestParam Integer internshipId, RedirectAttributes ra) {

		InternshipApplication app = applicationRepo.findById(appId).orElse(null);

		if (app == null) {
			ra.addFlashAttribute("error", "Application not found");
			return "redirect:/admin-applicants?id=" + internshipId;
		}
		Internships i = app.getInternship();
		if ("accept".equals(action)) {
			app.setStatus(ApplicationStatus.ACCEPTED);

			emailService.sendAcceptedMail(
			    app.getEmail(),
			    app.getFullName(),
			    app.getInternship().getTitle()
			);
			ra.addFlashAttribute("msg",
				 app.getFullName() +  "Student accepted successfully");

		}
		else if ("reject".equals(action)) {
			
			app.setStatus(ApplicationStatus.REJECTED);
		
			emailService.sendRejectionMail(
				    app.getEmail(),
				    app.getFullName(),
				    app.getInternship().getTitle(),
				    app.getInternship().getRole(),
				    app.getInternship().getType()
				);
			ra.addFlashAttribute("msg",
				   app.getFullName() + "Student rejected ");
		} 
		else if ("select".equals(action)) {

		    app.setStatus(ApplicationStatus.SELECTED);

		    applicationRepo.save(app);

		    ra.addFlashAttribute("msg", app.getFullName() + "Student moved to final selection");

		
			emailService.sendOfferLetter(app.getEmail(), app.getFullName(), i.getTitle(), i.getRole(), i.getType(),
					i.getLocation(), i.getStipend(), i.getDuration(), i.getStartDate());

			ra.addFlashAttribute("msg", app.getFullName() + "Student selected & offer sent");
		    return "redirect:/admin-final-selection?internshipId=" + internshipId;
			
		}
		applicationRepo.save(app);

		return "redirect:/admin-applicants?id=" + internshipId;
	}

	@PostMapping("/admin/toggle-test")
	public String toggleTest(@RequestParam Integer internshipId,
	                         @RequestParam(required = false) Boolean confirmRemove,
	                         RedirectAttributes ra) {

	    Internships i = internshipRepo.findById(internshipId).orElse(null);

	    if (i == null) {
	        ra.addFlashAttribute("error", "Internship not found ❌");
	        return "redirect:/manage-internships";
	    }

	    InternshipTest test = testRepo.findByInternshipId(internshipId);
	    // ================= ADD TEST =================
	    if (!i.getHasTest()) {
	        i.setHasTest(true);
	        internshipRepo.save(i);
	        ra.addFlashAttribute("msg", "✅ Test enabled. Now create/manage it");
	    }
	    // ================= REMOVE TEST =================
	    else {
	        // ❗ FIRST CLICK → ASK CONFIRMATION
	        if (confirmRemove == null || !confirmRemove) {
	            ra.addFlashAttribute("error",
	                    "⚠️ Are you sure you want to remove the test? Click again to confirm.");

	            return "redirect:/manage-internships";
	        }
	        // ✅ CONFIRMED REMOVE
	        if (test != null) {
	            testRepo.delete(test);
	        }

	        i.setHasTest(false);
	        internshipRepo.save(i);

	        ra.addFlashAttribute("msg", "❌ Test removed successfully");
	    }

	    return "redirect:/manage-internships";
	}
	
}