package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminChatController {

	@GetMapping("/admin-chat")
	public String adminChat(HttpSession session, Model model) {
		if (session.getAttribute("adminEmail") == null) {
			return "redirect:/admin-login";
		}

		String username = (String) session.getAttribute("adminUsername");
		model.addAttribute("username", username);
		return "admin-chat";
	}
}
