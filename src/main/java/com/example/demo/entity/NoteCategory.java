package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "note_category")
public class NoteCategory {

	 	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Integer categoryId;

	    @Column(unique = true, nullable = false)
	    private String name; // Java, UI/UX, Python

	    private boolean active = true;

		public Integer getCategoryId() {
			return categoryId;
		}

		public void setCategoryId(Integer categoryId) {
			this.categoryId = categoryId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}
	    
	    
}
