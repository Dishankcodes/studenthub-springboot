package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.TestAnswer;

public interface TestAnswerRepository extends JpaRepository<TestAnswer, Integer> {

	List<TestAnswer> findByAttempt_Id(Integer attemptId);

}
