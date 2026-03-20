package com.example.demo.entity;

import com.example.demo.enums.QuestionFormat;
import com.example.demo.enums.QuizQuestionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumeratedValue;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "quiz_question")
public class QuizQuestion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer questionId;

	@ManyToOne
	@JoinColumn(name = "quiz_id", nullable = true)
	private Quiz quiz;

	private String questionText;

	@Column(nullable = true)
	private String optionA;

	@Column(nullable = true)
	private String optionB;

	@Column(nullable = true)
	private String optionC;

	@Column(nullable = true)
	private String optionD;

	@Column(nullable = true)
	private String correctOption;
	
	@Column(nullable = false)
	private Integer marks;

	@Column(nullable = false)
	private Integer position;

	@Enumerated(EnumType.STRING)
	private QuestionFormat questionFormat; 
	
	@Enumerated(EnumType.STRING)
	private QuizQuestionType type;
	
	@ManyToOne
	@JoinColumn(name = "course_id")
	private Course course; // for teacher

	@ManyToOne
	@JoinColumn(name = "internship_id")
	private Internships internship;

	public Integer getQuestionId() {
		return questionId;
	}

	public void setQuestionId(Integer questionId) {
		this.questionId = questionId;
	}

	public Quiz getQuiz() {
		return quiz;
	}

	public void setQuiz(Quiz quiz) {
		this.quiz = quiz;
	}

	public String getQuestionText() {
		return questionText;
	}

	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}

	public String getOptionA() {
		return optionA;
	}

	public void setOptionA(String optionA) {
		this.optionA = optionA;
	}

	public String getOptionB() {
		return optionB;
	}

	public void setOptionB(String optionB) {
		this.optionB = optionB;
	}

	public String getOptionC() {
		return optionC;
	}

	public void setOptionC(String optionC) {
		this.optionC = optionC;
	}

	public String getOptionD() {
		return optionD;
	}

	public void setOptionD(String optionD) {
		this.optionD = optionD;
	}

	public String getCorrectOption() {
		return correctOption;
	}

	public void setCorrectOption(String correctOption) {
		this.correctOption = correctOption;
	}

	public Integer getMarks() {
		return marks;
	}

	public void setMarks(Integer marks) {
		this.marks = marks;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public QuestionFormat getQuestionFormat() {
		return questionFormat;
	}

	public void setQuestionFormat(QuestionFormat questionFormat) {
		this.questionFormat = questionFormat;
	}

	public QuizQuestionType getType() {
		return type;
	}

	public void setType(QuizQuestionType type) {
		this.type = type;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public Internships getInternship() {
		return internship;
	}

	public void setInternship(Internships internship) {
		this.internship = internship;
	}

}
