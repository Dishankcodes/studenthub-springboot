package com.example.demo.entity;

import java.time.LocalDateTime;

import com.example.demo.enums.AnnouncementAudience;
import com.example.demo.enums.AnnouncementType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;


@Entity
@Table(name = "announcement")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer announcementId;

    private String title;

    @Column(length = 3000)
    private String message;

    @Enumerated(EnumType.STRING)
    private AnnouncementType type;
    
    @Enumerated(EnumType.STRING)
    private AnnouncementAudience audience;

    // Nullable → only for COURSE announcements
    @ManyToOne
    private Course course;

    // Nullable → present if teacher created
    @ManyToOne
    private Teacher teacher;

    // Admin announcements → teacher = null
    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    private String attachmentUrl;
    private String attachmentName;
    
    private boolean pinned = false;
	public Integer getAnnouncementId() {
		return announcementId;
	}

	public void setAnnouncementId(Integer announcementId) {
		this.announcementId = announcementId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public AnnouncementType getType() {
		return type;
	}

	public void setType(AnnouncementType type) {
		this.type = type;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public Teacher getTeacher() {
		return teacher;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public AnnouncementAudience getAudience() {
		return audience;
	}

	public void setAudience(AnnouncementAudience audience) {
		this.audience = audience;
	}

	public String getAttachmentUrl() {
		return attachmentUrl;
	}

	public void setAttachmentUrl(String attachmentUrl) {
		this.attachmentUrl = attachmentUrl;
	}

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public boolean isPinned() {
		return pinned;
	}

	public void setPinned(boolean pinned) {
		this.pinned = pinned;
	}
    
    
    
    
    
}