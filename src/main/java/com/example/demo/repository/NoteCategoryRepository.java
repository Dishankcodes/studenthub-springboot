package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.NoteCategory;

public interface NoteCategoryRepository
extends JpaRepository<NoteCategory, Integer> {

List<NoteCategory> findByActiveTrue();
}
