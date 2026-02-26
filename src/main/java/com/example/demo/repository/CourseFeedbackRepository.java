package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.CourseFeedback;

public interface CourseFeedbackRepository
extends JpaRepository<CourseFeedback, Integer> {

boolean existsByCourseCourseIdAndStudentStudid(
    Integer courseId, Integer studentId);

List<CourseFeedback> findByCourseCourseId(Integer courseId);


long countByCourseCourseId(Integer courseId);

@Query("""
    SELECT COALESCE(AVG(f.rating), 0)
    FROM CourseFeedback f
    WHERE f.course.courseId = :courseId
""")
double findAverageRating(@Param("courseId") Integer courseId);

@Query("""
	    SELECT f
	    FROM CourseFeedback f
	    WHERE f.course.teacher.teacherId = :teacherId
	    ORDER BY f.createdAt DESC
	""")
	List<CourseFeedback> findByTeacherId(Integer teacherId);
}