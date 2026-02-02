package com.example.demo.entity;

import jakarta.persistence.*;


@Entity
@Table(name="student")
public class Student {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer studid;
	
	private String fullname;
	
	private String college;
	
	
	@Column(nullable = false ,unique = true)
	private String email;
	
	private String password;

	public Integer getStud_id() {
		return studid;
	}

	public void setStud_id(Integer studid) {
		this.studid = studid;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getCollege() {
		return college;
	}

	public void setCollege(String college) {
		this.college = college;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	 public void setPassword(String password) {
	        this.password = password;
	 }

	 public String getPassword() {
		    return password;
		}

	

	
}
