package com.example.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.ChatUser;
import com.example.demo.repository.ChatUserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminChatController {

	@Autowired
	private ChatUserRepository chatUserRepo;

	@GetMapping("/admin-chat")
	public String adminChat(HttpSession session, Model model) {

		if (session.getAttribute("adminEmail") == null)
			return "redirect:/admin-login";

		List<ChatUser> users = chatUserRepo.findAll();

		model.addAttribute("users", users);

		return "admin-chat";
	}
}