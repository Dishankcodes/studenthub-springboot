package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.TestAnswer;

public interface TestAnswerRepository extends JpaRepository<TestAnswer, Integer>{

}
