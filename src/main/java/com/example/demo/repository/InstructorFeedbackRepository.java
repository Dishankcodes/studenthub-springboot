package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.InstructorFeedback;

public interface InstructorFeedbackRepository extends JpaRepository<InstructorFeedback, Integer> {

	boolean existsByTeacherTeacherIdAndStudentStudid(Integer teacherId, Integer studentId);

	List<InstructorFeedback> findByTeacherTeacherId(Integer teacherId);

	@Query("""
			SELECT COALESCE(AVG(f.rating), 0)
			FROM InstructorFeedback f
			WHERE f.teacher.teacherId = :teacherId
			""")
	double getAverageRating(Integer teacherId);
}