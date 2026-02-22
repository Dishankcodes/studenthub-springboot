package com.example.demo.entity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.enums.CourseLevel;
import com.example.demo.enums.CourseStatus;
import com.example.demo.enums.CourseType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "course", uniqueConstraints = { @UniqueConstraint(columnNames = { "title", "teacher_id" }) })
public class Course {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer courseId;

	@ManyToOne(optional = false)
	@JoinColumn(name = "teacher_id", nullable = false)
	private Teacher teacher;

	@OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("position ASC")
	private Set<CourseModule> modules = new LinkedHashSet<>();

	
	@Transient
	@OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CourseHighlight> highlights = new ArrayList<>();

	@Column(nullable = false)
	private String title;

	private String description;

	private Double price;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CourseType type; // free paid

	@Enumerated(EnumType.STRING)
	private CourseLevel level; // adv , beg , inter

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CourseStatus status; // published , block , draft

	private String thumbnailURL;

	@Transient
	private MultipartFile thumbnailFile;

	public MultipartFile getThumbnailFile() {
		return thumbnailFile;
	}

	public void setThumbnailFile(MultipartFile thumbnailFile) {
		this.thumbnailFile = thumbnailFile;
	}

	public Integer getCourseId() {
		return courseId;
	}

	public void setCourseId(Integer courseId) {
		this.courseId = courseId;
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

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public CourseType getType() {
		return type;
	}

	public void setType(CourseType type) {
		this.type = type;
	}

	public CourseLevel getLevel() {
		return level;
	}

	public void setLevel(CourseLevel level) {
		this.level = level;
	}

	public CourseStatus getStatus() {
		return status;
	}

	public void setStatus(CourseStatus status) {
		this.status = status;
	}

	public String getThumbnailURL() {
		return thumbnailURL;
	}

	public void setThumbnailURL(String thumbnailURL) {
		this.thumbnailURL = thumbnailURL;
	}

	public Teacher getTeacher() {
		return teacher;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}

	public Set<CourseModule> getModules() {
		return modules;
	}

	public void setModules(Set<CourseModule> modules) {
		this.modules = modules;
	}

	public List<CourseHighlight> getHighlights() {
	    return highlights;
	}
	
	public void setHighlights(List<CourseHighlight> highlights) {
		this.highlights = highlights;
	}

}
