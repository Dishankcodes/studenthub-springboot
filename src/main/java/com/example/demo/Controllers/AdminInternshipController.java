package com.example.demo.Controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Admin;
import com.example.demo.entity.Course;
import com.example.demo.entity.Internships;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.InternshipRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminInternshipController {

    @Autowired
    private InternshipRepository internshipRepo;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private AdminRepository adminRepo;

    @GetMapping("/manage-internships")
    public String manageInternships(Model model , HttpSession session) {

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
    public String saveInternship(
            @RequestParam String title,
            @RequestParam String role,
            @RequestParam String type,
            @RequestParam String location,
            @RequestParam String skills,
            @RequestParam Integer stipend,
            @RequestParam String duration,
            @RequestParam String startDate,
            @RequestParam String description,
            @RequestParam(required = false) Integer courseId
    ) {

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
        i.setStartDate(LocalDate.parse(startDate));
        i.setDescription(description);
        i.setAdmin(admin);

        if (courseId != null) {
            Course course = courseRepo.findById(courseId).orElse(null);
            i.setRequiredCourse(course);
        }

        internshipRepo.save(i);

        return "redirect:/admin-post-internships";
    }
}