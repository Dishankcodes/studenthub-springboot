package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.CourseFeedback;

public interface CourseFeedbackRepository
extends JpaRepository<CourseFeedback, Integer> {

boolean existsByCourseCourseIdAndStudentStudid(
    Integer courseId, Integer studentId);

List<CourseFeedback> findByCourseCourseId(Integer courseId);
}