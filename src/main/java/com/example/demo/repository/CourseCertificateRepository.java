package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.CourseCertificate;

public interface CourseCertificateRepository extends JpaRepository<CourseCertificate, Integer>{

	boolean existsByStudentIdAndCourseCourseId(
			Integer studentId, Integer courseId);
	
	Optional<CourseCertificate>
	findByStudentIdAndCourseCourseId(Integer studentId, Integer courseId);
	
	List<CourseCertificate> findByStudentId(Integer studentId);
	
	Optional<CourseCertificate> findByCertificateNumber(String certificateNumber);
	
}
