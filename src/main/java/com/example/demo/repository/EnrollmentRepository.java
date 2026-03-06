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

	Optional<Enrollment> findByStudentStudidAndCourseCourseId(Integer studid, Integer courseId);

	@Query("""
			SELECT COUNT(DISTINCT e.student.studid)
			FROM Enrollment e
			WHERE e.course.teacher.teacherId = :teacherId
			""")
	long countDistinctStudentsByTeacher(Integer teacherId);

	@Query("""
			SELECT COALESCE(SUM(c.price), 0)
			FROM Enrollment e
			JOIN e.course c
			WHERE c.teacher.teacherId = :teacherId
			""")
	Double getTotalRevenueByTeacher(Integer teacherId);

	@Query("""
			SELECT MONTH(e.enrolledAt), COUNT(e)
			FROM Enrollment e
			JOIN e.course c
			WHERE c.teacher.teacherId = :teacherId
			GROUP BY MONTH(e.enrolledAt)
			ORDER BY MONTH(e.enrolledAt)
			""")
			List<Object[]> countStudentsGroupedByMonth(Integer teacherId);

			List<Enrollment> findByStudentStudid(Integer studid);

			@Query("""
					SELECT e.course.courseId
					FROM Enrollment e
					WHERE e.student.studid = :studentId
					AND e.completedAt IS NOT NULL
					""")
					List<Integer> findCompletedCourses(Integer studentId);
}
