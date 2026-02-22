package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Enrollment;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {

	
	 boolean existsByStudentIdAndCourseCourseId(
	            Integer studentId,
	            Integer courseId
	    );
}
