package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.ChatMessage;

import jakarta.mail.Message;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    List<ChatMessage> findByChatRoomIdOrderByTimestampAsc(Integer chatRoomId);
    long countByChatRoomIdAndSeenFalseAndSenderIdNot(Integer chatRoomId, Integer myId);
	
	
	ChatMessage findTop1ByChatRoomIdOrderByTimestampDesc(Integer chatRoomId);
}