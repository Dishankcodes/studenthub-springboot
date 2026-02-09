package com.example.demo.entity;


import com.example.demo.enums.LessonType;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "module_lesson")
public class Lesson {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer lessonId;
	
	@Column(nullable = false)
	private String title;

	@Enumerated(EnumType.STRING)
	private LessonType type;
	
	private String contentUrl;
	
	@Column(nullable = false)
	private Integer position;
	
	@Column(nullable = false)
	private boolean freePreview = false;
	
	@ManyToOne
	@JoinColumn(nullable = false ,  name = "module_id")
	private CourseModule module;

	
	@OneToOne(mappedBy = "lesson" ,cascade = CascadeType.ALL)
	private Quiz quiz; 
	
	
	public Integer getLessonId() {
		return lessonId;
	}

	public void setLessonId(Integer lessonId) {
		this.lessonId = lessonId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public LessonType getType() {
		return type;
	}

	public void setType(LessonType type) {
		this.type = type;
	}

	public String getContentUrl() {
		return contentUrl;
	}

	public void setContentUrl(String contentUrl) {
		this.contentUrl = contentUrl;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public boolean isFreePreview() {
		return freePreview;
	}

	public void setFreePreview(boolean freePreview) {
		this.freePreview = freePreview;
	}

	public CourseModule getModule() {
		return module;
	}

	public void setModule(CourseModule module) {
		this.module = module;
	}

	public Quiz getQuiz() {
		return quiz;
	}

	public void setQuiz(Quiz quiz) {
		this.quiz = quiz;
	}
	
	
}
