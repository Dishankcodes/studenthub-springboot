package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Connection;
import com.example.demo.enums.ConnectionStatus;

public interface ConnectionRepository extends JpaRepository<Connection, Integer> {

	// 🔍 find connection between two users

	Optional<Connection> findBySenderIdAndReceiverIdOrSenderIdAndReceiverId(Integer s1, Integer r1, Integer s2,
			Integer r2);

	// 👥 get all accepted connections
	List<Connection> findBySenderIdAndStatusOrReceiverIdAndStatus(Integer senderId, ConnectionStatus status1,
			Integer receiverId, ConnectionStatus status2);

	Optional<Connection> findBySenderIdAndReceiverId(Integer senderId, Integer receiverId);

	Optional<Connection> findByReceiverIdAndSenderId(Integer receiverId, Integer senderId);

	List<Connection> findByReceiverIdAndStatus(Integer receiverId, ConnectionStatus status);

	List<Connection> findBySenderIdAndStatus(Integer senderId, ConnectionStatus status);

	Long countByReceiverIdAndStatus(Integer id, ConnectionStatus pending);

	long countBySenderRefIdOrReceiverRefId(Integer id, Integer id2);
}