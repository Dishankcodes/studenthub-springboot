package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room")
public class ChatRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "user1_id")
	private ChatUser user1;

	@ManyToOne
	@JoinColumn(name = "user2_id")
	private ChatUser user2;

	@ManyToOne
	@JoinColumn(name = "course_id")
	private Course course;
	
	
	private LocalDateTime createdAt = LocalDateTime.now();

	
	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public ChatUser getUser1() {
		return user1;
	}

	public void setUser1(ChatUser user1) {
		this.user1 = user1;
	}

	public ChatUser getUser2() {
		return user2;
	}

	public void setUser2(ChatUser user2) {
		this.user2 = user2;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

}