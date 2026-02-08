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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "teacher")
public class Teacher {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer teacherId;

	private String firstname;

	private String lastname;

	@Column(nullable = false, unique = true)
	private String phoneno;

	@Column(nullable = false, unique = true)
	private String email;

	private String password;

	private String countryCode;

	@OneToOne(mappedBy = "teacher", cascade = CascadeType.ALL)
	private TeacherProfile profile;

	@OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
	private List<Course> teacherCourse = new ArrayList<>();

	public List<Course> getTeacherCourse() {
		return teacherCourse;
	}

	public void setTeacherCourse(List<Course> teacherCourse) {
		this.teacherCourse = teacherCourse;
	}

	public TeacherProfile getProfile() {
		return profile;
	}

	public void setProfile(TeacherProfile profile) {
		this.profile = profile;
	}

	public Integer getTeacherId() {
		return teacherId;
	}

	public void setTeacherId(Integer teacherId) {
		this.teacherId = teacherId;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getPhoneno() {
		return phoneno;
	}

	public void setPhoneno(String phoneno) {
		this.phoneno = phoneno;
	}

	public String getEmail() {
		return email;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
