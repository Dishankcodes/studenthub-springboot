package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Teacher;

public interface TeacherRepo extends JpaRepository<Teacher, Integer>{

	
	boolean existsByEmail(String email);
	boolean existsByPhoneno(String phoneno);
	
	Teacher findByPhoneno(String phoneno);
	Teacher findByemail(String email);
	
	
}
