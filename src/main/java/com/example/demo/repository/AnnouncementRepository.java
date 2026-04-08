package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Announcement;
import com.example.demo.enums.AnnouncementAudience;
import com.example.demo.enums.AnnouncementType;

public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {

   
    List<Announcement> findByActiveTrueOrderByCreatedAtDesc();

    List<Announcement> findTop5ByActiveTrueOrderByCreatedAtDesc();

    List<Announcement> findByActiveTrueOrderByPinnedDescCreatedAtDesc();


   
    List<Announcement> findByTypeInAndActiveTrueOrderByCreatedAtDesc(List<AnnouncementType> types);


   
    List<Announcement> findByCourseCourseIdAndActiveTrueOrderByCreatedAtDesc(Integer courseId);


   
    List<Announcement> findByTeacherTeacherIdAndActiveTrueOrderByCreatedAtDesc(Integer teacherId);


   
    List<Announcement> findByTeacherIsNullAndActiveTrueOrderByCreatedAtDesc();

    // Admin announcements filtered by audience ✅ FIXED
    List<Announcement> findByTeacherIsNullAndActiveTrueAndAudienceInOrderByCreatedAtDesc(
            List<AnnouncementAudience> audiences
    );


   
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

//
   
    @Query("""
        SELECT a FROM Announcement a
        WHERE a.active = true
        AND a.teacher IS NOT NULL
        AND a.teacher.teacherId != :teacherId
        ORDER BY a.pinned DESC, a.createdAt DESC
    """)
    List<Announcement> findOtherTeachersAnnouncements(@Param("teacherId") Integer teacherId);


   
    @Query("""
        SELECT a FROM Announcement a
        WHERE a.active = true
        AND a.audience IN :audiences
        ORDER BY a.createdAt DESC
    """)
    List<Announcement> findByAudienceList(@Param("audiences") List<AnnouncementAudience> audiences);

    
    @Query("""
    	    SELECT a FROM Announcement a
    	    WHERE a.active = true
    	    AND (
    	        a.audience = com.example.demo.enums.AnnouncementAudience.STUDENTS
    	        OR a.audience = com.example.demo.enums.AnnouncementAudience.ALL
    	        OR (
    	            a.audience = com.example.demo.enums.AnnouncementAudience.ENROLLED
    	            AND a.course.courseId IN :courseIds
    	        )
    	    )
    	    ORDER BY a.pinned DESC, a.createdAt DESC
    	""")
    	List<Announcement> findForStudentWithEnrollments(@Param("courseIds") List<Integer> courseIds);
}

