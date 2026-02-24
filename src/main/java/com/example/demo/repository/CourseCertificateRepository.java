package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.CourseCertificate;

public interface CourseCertificateRepository extends JpaRepository<CourseCertificate, Integer>{

	boolean existsByStudentStudidAndCourseCourseId(
			Integer studid, Integer courseId);
	
	Optional<CourseCertificate>
	findByStudentStudidAndCourseCourseId(Integer studid, Integer courseId);
	
	List<CourseCertificate> findByStudentStudid(Integer studid);
	
	Optional<CourseCertificate> findByCertificateNumber(String certificateNumber);
	
}
