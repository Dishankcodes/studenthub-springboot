package com.example.demo.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "Internships")
public class Internships {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;
    private String role;
    private String type;
    private String location;

    @Column(length = 1000)
    private String skills; 

    private Integer stipend;
    private String duration;
    private LocalDate startDate;

    @Column(length = 2000)
    private String description;


    @ManyToOne
    @JoinColumn(name = "course_id", nullable = true)
    private Course requiredCourse;

    // ADMIN (company)
    @ManyToOne
    private Admin admin;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getSkills() {
		return skills;
	}

	public void setSkills(String skills) {
		this.skills = skills;
	}

	public Integer getStipend() {
		return stipend;
	}

	public void setStipend(Integer stipend) {
		this.stipend = stipend;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Course getRequiredCourse() {
		return requiredCourse;
	}

	public void setRequiredCourse(Course requiredCourse) {
		this.requiredCourse = requiredCourse;
	}

	public Admin getAdmin() {
		return admin;
	}

	public void setAdmin(Admin admin) {
		this.admin = admin;
	}

    
    
}