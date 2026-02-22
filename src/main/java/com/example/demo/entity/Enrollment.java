package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "enrollment")
public class Enrollment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer enrollmentId;

	  private Integer studentId;

	    @ManyToOne
	    private Course course;

	    private LocalDateTime enrolledAt = LocalDateTime.now();

		public Integer getEnrollmentId() {
			return enrollmentId;
		}

		public void setEnrollmentId(Integer enrollmentId) {
			this.enrollmentId = enrollmentId;
		}

		public Integer getStudentId() {
			return studentId;
		}

		public void setStudentId(Integer studentId) {
			this.studentId = studentId;
		}

		public Course getCourse() {
			return course;
		}

		public void setCourse(Course course) {
			this.course = course;
		}

		public LocalDateTime getEnrolledAt() {
			return enrolledAt;
		}

		public void setEnrolledAt(LocalDateTime enrolledAt) {
			this.enrolledAt = enrolledAt;
		}
	    
	    
	    
	    
}
