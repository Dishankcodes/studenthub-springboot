package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "enrollment")
public class Enrollment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer enrollmentId;

	@ManyToOne
	@JoinColumn(name = "student_id")
	private Student student;

	

    @ManyToOne
    @JoinColumn(name = "course_course_id", nullable = false)
    private Course course;

	    private LocalDateTime enrolledAt = LocalDateTime.now();

		public Integer getEnrollmentId() {
			return enrollmentId;
		}

		public void setEnrollmentId(Integer enrollmentId) {
			this.enrollmentId = enrollmentId;
		}

	

		public Student getStudent() {
			return student;
		}

		public void setStudent(Student student) {
			this.student = student;
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
