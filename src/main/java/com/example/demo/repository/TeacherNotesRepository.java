package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.TeacherNotes;
import com.example.demo.enums.NoteStatus;

public interface TeacherNotesRepository extends JpaRepository<TeacherNotes, Integer>{

	List<TeacherNotes> findByApprovedTrueOrderByUploadedAtDesc();

	List<TeacherNotes> findByTeacherTeacherId(Integer teacherId);
	
	
	@Query("""
		    SELECT n FROM TeacherNotes n
		    WHERE n.status = com.example.demo.enums.NoteStatus.APPROVED
		     AND n.category.active = true
		    AND (:category IS NULL OR n.category.categoryId = :category)
		    AND (:q IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :q, '%')))
		    ORDER BY n.uploadedAt DESC
		""")
		List<TeacherNotes> search(
		    @Param("category") Integer category,
		    @Param("q") String q
		);
	
	@Query("""
			SELECT n FROM TeacherNotes n
			WHERE n.status = 'PENDING'
			AND n.uploadedAt <= :time
			""")
			List<TeacherNotes> findPendingBefore(LocalDateTime time);


	List<TeacherNotes> findByStatus(NoteStatus status);

}
