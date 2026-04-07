package com.example.demo.entity;

import java.util.Objects;

import com.example.demo.enums.UserType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_user")
public class ChatUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer refId;

	@Enumerated(EnumType.STRING)
	private UserType type; // STUDENT / TEACHER / ADMIN

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ChatUser))
			return false;
		ChatUser other = (ChatUser) o;
		return refId.equals(other.refId) && type == other.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(refId, type);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getRefId() {
		return refId;
	}

	public void setRefId(Integer refId) {
		this.refId = refId;
	}

	public UserType getType() {
		return type;
	}

	public void setType(UserType type) {
		this.type = type;
	}

}