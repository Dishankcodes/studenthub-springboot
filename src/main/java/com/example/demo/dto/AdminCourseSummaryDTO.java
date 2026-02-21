package com.example.demo.dto;

import com.example.demo.enums.CourseStatus;
import com.example.demo.enums.CourseType;

public class AdminCourseSummaryDTO {

	private Integer courseId;
	private String title;
	private String instructorName;
	private CourseStatus status;
	private CourseType type;
	private Double price;
	private Long moduleCount;
	private Long lessonCount;
	private Long quizCount;
	

	public AdminCourseSummaryDTO(Integer courseId, String title, String instructorName, CourseStatus status,
			CourseType type, Double price, Long moduleCount, Long lessonCount, Long quizCount) {
		this.courseId = courseId;
		this.title = title;
		this.instructorName = instructorName;
		this.status = status;
		this.type = type;
		this.price = price;
		this.moduleCount = moduleCount;
		this.lessonCount = lessonCount;
		this.quizCount = quizCount;
	}

	public Integer getCourseId() {
		return courseId;
	}

	public String getTitle() {
		return title;
	}

	public String getInstructorName() {
		return instructorName;
	}

	public CourseStatus getStatus() {
		return status;
	}

	public CourseType getType() {
		return type;
	}

	public Double getPrice() {
		return price;
	}

	public Long getModuleCount() {
		return moduleCount;
	}

	public Long getLessonCount() {
		return lessonCount;
	}

	public Long getQuizCount() {
		return quizCount;
	}
}