package com.example.demo.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "student")
public class Student {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer studid;

	@NotBlank(message ="Full Name Required")
	private String fullname;

	private String college;

	@Column(nullable = false, unique = true)
	private String email;

	private String password;

	
	 // 🔥 Enrollments
    @OneToMany(mappedBy = "student",
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<Enrollment> enrollments = new ArrayList<>();

    // 🔥 Lesson Progress
    @OneToMany(mappedBy = "student",
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<LessonProgress> lessonProgressList = new ArrayList<>();

	public Integer getStudid() {
		return studid;
	}

	public void setStudid(Integer studid) {
		this.studid = studid;
	}

	public List<Enrollment> getEnrollments() {
		return enrollments;
	}

	public void setEnrollments(List<Enrollment> enrollments) {
		this.enrollments = enrollments;
	}

	public List<LessonProgress> getLessonProgressList() {
		return lessonProgressList;
	}

	public void setLessonProgressList(List<LessonProgress> lessonProgressList) {
		this.lessonProgressList = lessonProgressList;
	}



	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getCollege() {
		return college;
	}

	public void setCollege(String college) {
		this.college = college;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

}
