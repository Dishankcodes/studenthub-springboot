package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class InternshipTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    private Internships internship;

    private int durationMinutes;
    private int passingMarks;
    
    private int totalQuestionsToShow;
    
    
	public int getTotalQuestionsToShow() {
		return totalQuestionsToShow;
	}
	public void setTotalQuestionsToShow(int totalQuestionsToShow) {
		this.totalQuestionsToShow = totalQuestionsToShow;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Internships getInternship() {
		return internship;
	}
	public void setInternship(Internships internship) {
		this.internship = internship;
	}
	public int getDurationMinutes() {
		return durationMinutes;
	}
	public void setDurationMinutes(int durationMinutes) {
		this.durationMinutes = durationMinutes;
	}
	public int getPassingMarks() {
		return passingMarks;
	}
	public void setPassingMarks(int passingMarks) {
		this.passingMarks = passingMarks;
	}
    
    
}