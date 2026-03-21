package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.CertificateTemplate;
import com.example.demo.enums.CertificateType;

public interface CertificateTemplateRepository extends JpaRepository<CertificateTemplate, Integer> {

	Optional<CertificateTemplate> findByTypeAndActiveTrue(CertificateType type);

	List<CertificateTemplate> findByType(CertificateType type);

}
