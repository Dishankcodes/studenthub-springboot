package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.ChatUser;
import com.example.demo.enums.UserType;

public interface ChatUserRepository extends JpaRepository<ChatUser, Integer> {

    Optional<ChatUser> findByRefIdAndType(Integer refId, UserType type);
//    List<ChatUser> findByNameContainingIgnoreCase(String name);
    
    
}