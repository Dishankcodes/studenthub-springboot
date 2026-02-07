package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.TeacherProfile;

public interface TeacherProfileRepo extends JpaRepository<TeacherProfile, Integer>{


	TeacherProfile findByTeacherTeacherId(Integer teacherId);

}
