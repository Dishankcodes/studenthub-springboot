package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

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
}