package com.example.demo.entity;

import java.time.LocalDateTime;

import com.example.demo.enums.NoteStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "teacher_notes")
public class TeacherNotes {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer noteId;

	private String title;

	@Column(length = 2000)
	private String description;

	private String fileUrl;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NoteStatus status = NoteStatus.PENDING;

	private LocalDateTime uploadedAt;
	private LocalDateTime approvedAt;
	@ManyToOne
	private Teacher teacher;

	private boolean approved = false;

	@ManyToOne
	private NoteCategory category;

	public NoteCategory getCategory() {
		return category;
	}

	public void setCategory(NoteCategory category) {
		this.category = category;
	}

	public Integer getNoteId() {
		return noteId;
	}

	public void setNoteId(Integer noteId) {
		this.noteId = noteId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@PrePersist
	public void onCreate() {
	    this.uploadedAt = LocalDateTime.now();
	    if (this.status == null) {
	        this.status = NoteStatus.PENDING;
	    }
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public LocalDateTime getUploadedAt() {
		return uploadedAt;
	}

	public void setUploadedAt(LocalDateTime uploadedAt) {
		this.uploadedAt = uploadedAt;
	}

	public Teacher getTeacher() {
		return teacher;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	public NoteStatus getStatus() {
		return status;
	}

	public void setStatus(NoteStatus status) {
		this.status = status;
	}

	public LocalDateTime getApprovedAt() {
		return approvedAt;
	}

	public void setApprovedAt(LocalDateTime approvedAt) {
		this.approvedAt = approvedAt;
	}

}
