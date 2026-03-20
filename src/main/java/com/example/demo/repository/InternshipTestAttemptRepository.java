package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.InternshipTestAttempt;

public interface InternshipTestAttemptRepository extends JpaRepository<InternshipTestAttempt, Integer> {

InternshipTestAttempt findByStudentStudidAndInternshipId(Integer studentId, Integer internshipId);
}