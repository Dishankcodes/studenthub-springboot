package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "test_answer")
public class TestAnswer {

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Integer id;

	    @ManyToOne
	    private InternshipTestAttempt attempt;

	    @ManyToOne
	    private QuizQuestion question;

	    private String answerText; // for TEXT / CODE

	    private String selectedOption; // for MCQ

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public InternshipTestAttempt getAttempt() {
			return attempt;
		}

		public void setAttempt(InternshipTestAttempt attempt) {
			this.attempt = attempt;
		}

		public QuizQuestion getQuestion() {
			return question;
		}

		public void setQuestion(QuizQuestion question) {
			this.question = question;
		}

		public String getAnswerText() {
			return answerText;
		}

		public void setAnswerText(String answerText) {
			this.answerText = answerText;
		}

		public String getSelectedOption() {
			return selectedOption;
		}

		public void setSelectedOption(String selectedOption) {
			this.selectedOption = selectedOption;
		}
	    
	    
	
}
