package com.example.demo.entity;

import com.example.demo.enums.ApplicationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "internship_application")
public class InternshipApplication {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "student_studid")
	private Student student;

	@ManyToOne
	@JoinColumn(name = "internship_id")
	private Internships internship;

	@Enumerated(EnumType.STRING)
	private ApplicationStatus status;

	private String fullName;
	private String email;
	private String phone;

	private String resumeUrl;

	@Column(length = 1000)
	private String coverLetter;

	private boolean badgeGiven;

	private boolean certificateGenerated;
	private Integer certificateTemplateId;

	private String badgeTitle;

	private boolean allowReattempt;

	public boolean isAllowReattempt() {
		return allowReattempt;
	}

	public void setAllowReattempt(boolean allowReattempt) {
		this.allowReattempt = allowReattempt;
	}

	public String getBadgeTitle() {
		return badgeTitle;
	}

	public void setBadgeTitle(String badgeTitle) {
		this.badgeTitle = badgeTitle;
	}

	public Integer getCertificateTemplateId() {
		return certificateTemplateId;
	}

	public void setCertificateTemplateId(Integer certificateTemplateId) {
		this.certificateTemplateId = certificateTemplateId;
	}

	public boolean isBadgeGiven() {
		return badgeGiven;
	}

	public void setBadgeGiven(boolean badgeGiven) {
		this.badgeGiven = badgeGiven;
	}

	public boolean isCertificateGenerated() {
		return certificateGenerated;
	}

	public void setCertificateGenerated(boolean certificateGenerated) {
		this.certificateGenerated = certificateGenerated;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	public Internships getInternship() {
		return internship;
	}

	public void setInternship(Internships internship) {
		this.internship = internship;
	}

	public ApplicationStatus getStatus() {
		return status;
	}

	public void setStatus(ApplicationStatus status) {
		this.status = status;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getResumeUrl() {
		return resumeUrl;
	}

	public void setResumeUrl(String resumeUrl) {
		this.resumeUrl = resumeUrl;
	}

	public String getCoverLetter() {
		return coverLetter;
	}

	public void setCoverLetter(String coverLetter) {
		this.coverLetter = coverLetter;
	}

}