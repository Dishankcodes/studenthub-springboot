package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.Announcement;
import com.example.demo.enums.AnnouncementAudience;
import com.example.demo.enums.AnnouncementType;

public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {

    List<Announcement> findByTypeInAndActiveTrueOrderByCreatedAtDesc(
            List<AnnouncementType> types
    );

    List<Announcement> findByCourseCourseIdAndActiveTrueOrderByCreatedAtDesc(
            Integer courseId
    );

    List<Announcement> findByActiveTrueOrderByCreatedAtDesc();

    List<Announcement> findByTeacherTeacherIdAndActiveTrueOrderByCreatedAtDesc(
            Integer teacherId
    );

    List<Announcement> findByAudienceAndActiveTrueOrderByCreatedAtDesc(
            AnnouncementAudience audience
    );

    List<Announcement> findByActiveTrueOrderByPinnedDescCreatedAtDesc();
    
    @Query("""
    	    SELECT a FROM Announcement a
    	    WHERE a.active = true
    	    AND (a.audience = com.example.demo.enums.AnnouncementAudience.STUDENTS
    	         OR a.audience = com.example.demo.enums.AnnouncementAudience.ALL)
    	    ORDER BY a.pinned DESC, a.createdAt DESC
    	""")
    	List<Announcement> findForStudents();


    	@Query("""
    	    SELECT a FROM Announcement a
    	    WHERE a.active = true
    	    AND (a.audience = com.example.demo.enums.AnnouncementAudience.TEACHERS
    	         OR a.audience = com.example.demo.enums.AnnouncementAudience.ALL)
    	    ORDER BY a.pinned DESC, a.createdAt DESC
    	""")
    	List<Announcement> findForTeachers();
    	
    	List<Announcement> findByTeacherIsNullAndActiveTrueOrderByCreatedAtDesc();
    	
    	List<Announcement> findTop5ByActiveTrueOrderByCreatedAtDesc();

}