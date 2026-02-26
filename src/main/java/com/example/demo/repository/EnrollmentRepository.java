package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.Enrollment;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {

	
	boolean existsByStudentStudidAndCourseCourseId(Integer studid, Integer courseId);
	
	
	
	 @Query("""
		        SELECT e
		        FROM Enrollment e
		        WHERE e.course.teacher.teacherId = :teacherId
		        ORDER BY e.enrolledAt DESC
		    """)
		    List<Enrollment> findByTeacherId(Integer teacherId);
	 
	 
	 Optional<Enrollment> findByStudentStudidAndCourseCourseId(
		        Integer studid,
		        Integer courseId
		);
	 
	 
}
