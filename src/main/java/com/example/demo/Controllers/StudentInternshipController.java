package com.example.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.InternshipApplication;
import com.example.demo.entity.Internships;
import com.example.demo.entity.Student;
import com.example.demo.enums.ApplicationStatus;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.InternshipRepository;
import com.example.demo.repository.StudentRepository;

@Controller
public class StudentInternshipController {

    @Autowired
    private InternshipRepository internshipRepo;

    @Autowired
    private ApplicationRepository applicationRepo;

    @Autowired
    private StudentRepository studentRepo;

    @Autowired
    private EnrollmentRepository enrollmentRepo;

    
    @GetMapping("/student-internships")
    public String studentInternships(Model model) {

        List<Internships> internships = internshipRepo.findAll();

        model.addAttribute("internships", internships);

        return "student-internships";
    }
    
    @GetMapping("/student-internship-detail")
    public String internshipDetail(@RequestParam Integer id, Model model) {

        Internships internship = internshipRepo.findById(id).orElse(null);

        model.addAttribute("internship", internship);

        return "student-internship-detail";
    }
    
    @PostMapping("/student/apply")
    public String applyInternship(
            @RequestParam Integer internshipId,
            RedirectAttributes redirectAttributes
    ) {

        Integer studentId = 1;

        Internships internship = internshipRepo.findById(internshipId).orElse(null);
        Student student = studentRepo.findById(studentId).orElse(null);

        // ❌ already applied
        boolean exists = applicationRepo
                .existsByStudentStudidAndInternshipId(studentId, internshipId);

        if (exists) {
            redirectAttributes.addFlashAttribute("error", "You already applied for this internship.");
            return "redirect:/student-internship-detail?id=" + internshipId;
        }

        // 🔥 COURSE CHECK
        if (internship.getRequiredCourse() != null) {

            List<Integer> completedCourses =
                    enrollmentRepo.findCompletedCourses(studentId);

            boolean completed = completedCourses.contains(
                    internship.getRequiredCourse().getCourseId()
            );

            if (!completed) {
                redirectAttributes.addFlashAttribute("error",
                        "You must complete the required course first.");

                return "redirect:/student-internship-detail?id=" + internshipId;
            }
        }

        // ✅ SAVE
        InternshipApplication app = new InternshipApplication();
        app.setStudent(student);
        app.setInternship(internship);
        app.setStatus(ApplicationStatus.PENDING);

        applicationRepo.save(app);

        redirectAttributes.addFlashAttribute("success", "Application submitted successfully 🚀");

        return "redirect:/student-internship-detail?id=" + internshipId;
    }
}