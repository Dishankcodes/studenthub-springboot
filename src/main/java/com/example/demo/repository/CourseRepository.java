package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.dto.AdminCourseSummaryDTO;
import com.example.demo.entity.Course;
import com.example.demo.enums.CourseStatus;

public interface CourseRepository extends JpaRepository<Course, Integer> {

	//
	List<Course> findByTeacherTeacherId(Integer teacherId);

	List<Course> findByStatus(CourseStatus status);

	List<Course> findByTeacherTeacherIdAndStatusNot(Integer teacherId, CourseStatus status);

	boolean existsByTeacherTeacherIdAndTitleAndStatusNot(Integer teacherId, String title, CourseStatus status);

	@Query("""
			    SELECT DISTINCT c FROM Course c
			    LEFT JOIN FETCH c.modules m
			    LEFT JOIN FETCH m.lessons l
			    LEFT JOIN FETCH l.quiz q
			""")
	Course findAllWithStructure(Integer id);

	@Query("""
			SELECT new com.example.demo.dto.AdminCourseSummaryDTO(
			    c.courseId,
			    c.title,
			    CONCAT(t.firstname, ' ', t.lastname),
			    c.status,
			    c.type,
			    c.price,
			    COUNT(DISTINCT m.moduleId),
			    COUNT(DISTINCT l.lessonId),
			    COUNT(DISTINCT q.quizId)
			)
			FROM Course c
			LEFT JOIN c.teacher t
			LEFT JOIN c.modules m
			LEFT JOIN m.lessons l
			LEFT JOIN l.quiz q
			GROUP BY
			    c.courseId,
			    c.title,
			    t.firstname,
			    t.lastname,
			    c.status,
			    c.type,
			    c.price
			""")
	List<AdminCourseSummaryDTO> fetchAdminCourseSummary();
}
