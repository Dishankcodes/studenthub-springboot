package com.example.demo.Controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Service.EmailService;
import com.example.demo.entity.Admin;
import com.example.demo.entity.Course;
import com.example.demo.entity.InternshipApplication;
import com.example.demo.entity.Internships;
import com.example.demo.entity.Student;
import com.example.demo.enums.ApplicationStatus;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.InternshipRepository;
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
			@RequestParam String description, @RequestParam(required = false) Integer courseId) {

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

		return "redirect:/admin-post-internships";
	}
	
	
	@GetMapping("/admin-applicants")
	public String viewApplicants(@RequestParam Integer id, Model model) {

	    Internships internship = internshipRepo.findById(id).orElse(null);

	    List<InternshipApplication> applications =
	            applicationRepo.findByInternshipId(id); 

	    model.addAttribute("internship", internship);
	    model.addAttribute("applications", applications);

	    return "manage-applicants";
	}
	
	@GetMapping("/admin-view-student")
	public String viewStudent(@RequestParam Integer id, Model model) {

	    Student student = studentRepo.findById(id).orElse(null);

	    model.addAttribute("student", student);

	    return "admin-student-profile";
	}
	
	 @PostMapping("/admin/application/update")
	    public String updateApplication(
	            @RequestParam Integer appId,
	            @RequestParam String action,
	            @RequestParam Integer internshipId
	    ) {

	        InternshipApplication app =
	                applicationRepo.findById(appId).orElse(null);

	        if (app == null) {
	            return "redirect:/manage-internships";
	        }

	        if ("accept".equals(action)) {

	            app.setStatus(ApplicationStatus.ACCEPTED);

	            // 🔥 SEND OFFER EMAIL
	            emailService.sendOfferLetter(
	                    app.getEmail(),         // snapshot email
	                    app.getFullName(),
	                    app.getInternship().getTitle()
	            );

	        } else if ("reject".equals(action)) {

	            app.setStatus(ApplicationStatus.REJECTED);

	            // 🔥 SEND REJECTION EMAIL
	            emailService.sendRejectionMail(
	                    app.getEmail(),
	                    app.getFullName(),
	                    app.getInternship().getTitle()
	            );
	        }

	        applicationRepo.save(app);

	        return "redirect:/admin-applicants?id=" + internshipId;
	    }
}