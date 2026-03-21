package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.InternshipCertificate;

public interface InternshipCertificateRepository extends JpaRepository<InternshipCertificate, Integer> {

	Optional<InternshipCertificate> findByStudentStudidAndInternshipId(Integer studid, Integer internshipId);
}