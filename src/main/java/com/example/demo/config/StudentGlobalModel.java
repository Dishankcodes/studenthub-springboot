package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.entity.Student;
import com.example.demo.repository.StudentRepository;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class StudentGlobalModel {

    @Autowired
    private StudentRepository studentRepo;

    @ModelAttribute("loggedStudent")
    public Student loggedStudent(HttpSession session) {

        Integer studentId = (Integer) session.getAttribute("studentId");
        if (studentId == null) return null;

        return studentRepo.findById(studentId).orElse(null);
    }
}