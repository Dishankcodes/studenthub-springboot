package com.example.demo.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.InternshipTest;

public interface InternshipTestRepository extends JpaRepository<InternshipTest, Integer>{


	InternshipTest findByInternshipId(Integer internshipId);
}
