package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {
    @Query("""
        SELECT r FROM ChatRoom r 
        WHERE (r.user1.id = :u1 AND r.user2.id = :u2)
           OR (r.user1.id = :u2 AND r.user2.id = :u1)
    """)
    Optional<ChatRoom> findChatRoom(Integer u1, Integer u2);

	List<ChatRoom> findByUser1IdOrUser2Id(Integer id, Integer id2);
}