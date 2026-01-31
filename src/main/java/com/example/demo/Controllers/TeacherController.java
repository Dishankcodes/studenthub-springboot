package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TeacherController {

    // ===== DASHBOARD =====
    @GetMapping("/teacher-dashboard")
    public String teacherDashboard() {
        return "teacher-dashboard";
    }

    // ===== COURSE MANAGEMENT =====
    @GetMapping("/teacher-course")
    public String courseManagement() {
        return "teacher-courses";
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
    public String profilePage() {
        return "teacher-profile";
    }
    
}
