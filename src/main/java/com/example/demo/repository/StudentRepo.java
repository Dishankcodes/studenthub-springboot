package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Student;

public interface StudentRepo extends JpaRepository<Student, Integer> {

	boolean existsByEmail(String email);
	Optional<Student> findByEmail(String email);
}
