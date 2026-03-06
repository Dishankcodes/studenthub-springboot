package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
			    SELECT DISTINCT c
			    FROM Course c
			    LEFT JOIN FETCH c.modules m
			    LEFT JOIN FETCH m.lessons l
			    LEFT JOIN FETCH l.quiz
			    WHERE c.id = :id
			""")

	List<Course> findAllWithStructure(@Param("id") Integer id);

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

	@Query("""
			    SELECT DISTINCT c
			    FROM Course c
			    LEFT JOIN FETCH c.modules m
			    LEFT JOIN FETCH m.lessons l
			    WHERE c.courseId = :courseId
			      AND c.status = :status
			""")
	Course findPublishedCourseForStudent(@Param("courseId") Integer courseId, @Param("status") CourseStatus status);

	long countByTeacherTeacherId(Integer teacherId);

	long countByTeacherTeacherIdAndPriceIsNull(Integer teacherId);

	long countByTeacherTeacherIdAndPriceGreaterThan(Integer teacherId, Double price);

	@Query("""
			SELECT c.title, COUNT(e), AVG(f.rating)
			FROM Course c
			JOIN c.enrollments e
			LEFT JOIN CourseFeedback f ON f.course = c
			WHERE c.teacher.teacherId = :teacherId
			GROUP BY c.courseId, c.title
			ORDER BY COUNT(e) DESC
			""")
	List<Object[]> findTopCoursesByEnrollment(Integer teacherId);

	@Query("""
			SELECT c.title,
			       COUNT(e),
			       COALESCE(SUM(c.price), 0)
			FROM Course c
			LEFT JOIN c.enrollments e
			WHERE c.teacher.teacherId = :teacherId
			GROUP BY c.courseId, c.title
			""")
			List<Object[]> getCourseIncomeStats(Integer teacherId);
			
			@Query("""
					SELECT c
					FROM Course c
					LEFT JOIN Enrollment e ON e.course.courseId = c.courseId
					WHERE c.status = 'PUBLISHED'
					GROUP BY c
					ORDER BY COUNT(e) DESC
					""")
					List<Course> findPopularCourses();
}
