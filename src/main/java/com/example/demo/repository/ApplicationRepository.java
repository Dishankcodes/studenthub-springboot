package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.InternshipApplication;

public interface ApplicationRepository extends JpaRepository<InternshipApplication, Integer> {

    boolean existsByStudentStudidAndInternshipId(Integer studentId, Integer internshipId);
}