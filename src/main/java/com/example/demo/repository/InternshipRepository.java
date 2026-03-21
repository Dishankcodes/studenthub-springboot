package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Internships;

public interface InternshipRepository extends JpaRepository<Internships, Integer> {

}
