package com.example.demo.entity;

import com.example.demo.enums.CourseLevel;
import com.example.demo.enums.CourseStatus;
import com.example.demo.enums.CourseType;

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
import jakarta.persistence.UniqueConstraint;
	
@Entity
@Table (name = "course",
		uniqueConstraints = {
				@UniqueConstraint(columnNames = {"title","teacher_id"})
	}
)
public class Course {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer course_id;
	
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
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "teacher_id", nullable = false)
	private Teacher teacher;

	
}
