package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, Integer>{

	
	List<Lesson> findByModuleModuleIdOrderByPosition(Integer moduleId);
	
	List<Lesson> findByModuleModuleIdAndFreePreviewTrue(Integer moduleId);
	int countByModuleModuleId(Integer moduleId);
}
