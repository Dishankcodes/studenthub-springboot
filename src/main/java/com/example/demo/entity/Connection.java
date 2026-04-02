package com.example.demo.entity;

import com.example.demo.enums.ConnectionStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "connection")
public class Connection {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "sender_id")
	private ChatUser sender;

	@ManyToOne
	@JoinColumn(name = "receiver_id")
	private ChatUser receiver;

	@Enumerated(EnumType.STRING)
	private ConnectionStatus status; // PENDING / ACCEPTED / BLOCKED

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public ChatUser getSender() {
		return sender;
	}

	public void setSender(ChatUser sender) {
		this.sender = sender;
	}

	public ChatUser getReceiver() {
		return receiver;
	}

	public void setReceiver(ChatUser receiver) {
		this.receiver = receiver;
	}

	public ConnectionStatus getStatus() {
		return status;
	}

	public void setStatus(ConnectionStatus status) {
		this.status = status;
	}
}