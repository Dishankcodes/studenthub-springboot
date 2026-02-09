package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.QuizQuestion;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Integer>{
	
	List<QuizQuestion> findByQuizQuizIdOrderByPosition(Integer quizId);
}
