package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.CourseModule;

public interface CourseModuleRepository extends JpaRepository<CourseModule, Integer>{

	
	List<CourseModule> findByCourseCourseIdOrderByPosition(Integer courseId);

}
