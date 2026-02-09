package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Course;
import com.example.demo.enums.CourseStatus;


public interface CourseRepository extends JpaRepository<Course, Integer>{

	//
	List<Course> findByTeacherTeacherId(Integer teacherId);
	
	List<Course> findByStatus(CourseStatus status);
	
}
