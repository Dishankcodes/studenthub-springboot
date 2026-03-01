package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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

	    private LocalDateTime uploadedAt;

	    @ManyToOne
	    private Teacher teacher;

	    private boolean approved = true;

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
	    
	    
	
}
