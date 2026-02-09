package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Integer>{

	
	Optional<Quiz> findByLessonLessonId(Integer lessonId);
}
