package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.CourseCertificate;

public interface CourseCertificateRepository extends JpaRepository<CourseCertificate, Integer>{

	boolean existsByStudentIdAndCourseCourseId(
			Integer studentId, Integer courseId);
	
	CourseCertificate findByStudentIdAndCourseCourseId(
			Integer studentId, Integer courseId);
	
}
