package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.LessonProgress;

public interface LessonProgressRepository
extends JpaRepository<LessonProgress, Integer> {

	boolean existsByStudentStudidAndLessonLessonId(
		    Integer studentId,
		    Integer lessonId
		);

		long countByStudentStudidAndLessonModuleCourseCourseIdAndCompletedTrue(
		    Integer studentId,
		    Integer courseId
		);
		@Query("""
			    select lp.lesson.lessonId
			    from LessonProgress lp
			    where lp.student.studid = :studentId
			      and lp.completed = true
			      and lp.lesson.module.course.courseId = :courseId
			""")
		List<Integer> findCompletedLessonIds(
        @Param("studentId") Integer studentId,
        @Param("courseId") Integer courseId
    );
}