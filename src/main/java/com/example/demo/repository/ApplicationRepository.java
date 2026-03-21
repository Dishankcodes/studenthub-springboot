package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.InternshipApplication;
import com.example.demo.enums.ApplicationStatus;

public interface ApplicationRepository extends JpaRepository<InternshipApplication, Integer> {

	boolean existsByStudent_StudidAndInternship_Id(Integer studentId, Integer internshipId);
    @Query("""
    	    SELECT a FROM InternshipApplication a
    	    WHERE a.student.studid = :studentId
    	    AND a.internship.id = :internshipId
    	""")
    	Optional<InternshipApplication> findApplication(Integer studentId, Integer internshipId);
    
    
    List<InternshipApplication> findByInternshipId(Integer internshipId);

    List<InternshipApplication> findByStudent_Studid(Integer studentId);
    
    Optional<InternshipApplication> findByStudent_StudidAndInternship_Id(Integer studentId, Integer internshipId);
    
    List<InternshipApplication> findByInternship_IdAndStatus(Integer internshipId, ApplicationStatus status);
    

}