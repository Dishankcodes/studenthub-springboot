package com.example.demo.Controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.ChatUser;
import com.example.demo.enums.UserType;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.repository.ChatUserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminChatController {

	@Autowired
	private ChatUserRepository chatUserRepo;
	
	@Autowired
	private ChatRoomRepository chatRoomRepo;
	
	@Autowired
	private ChatMessageRepository messageRepo;

	@GetMapping("/admin-chat")
	public String adminChat(@RequestParam(required = false) Integer userId, HttpSession session, Model model) {

		if (session.getAttribute("adminEmail") == null) {
			return "redirect:/admin-login";
		}

		String username = (String) session.getAttribute("adminUsername");

		List<ChatUser> users = chatUserRepo.findAll();

		ChatUser selectedUser = null;
		List<ChatMessage> messages = new ArrayList<>();
		Integer roomId = null;

		if (userId != null) {

			selectedUser = chatUserRepo.findById(userId).orElse(null);

			if (selectedUser != null) {

				ChatUser admin = chatUserRepo.findByRefIdAndType(1, UserType.ADMIN) 
						.orElseThrow();

				Optional<ChatRoom> existingRoom = chatRoomRepo.findChatRoom(admin.getId(), selectedUser.getId());

				ChatRoom room;

				if (existingRoom.isPresent()) {
					room = existingRoom.get();
				} else {
					room = new ChatRoom();
					room.setUser1(admin);
					room.setUser2(selectedUser);
					room = chatRoomRepo.save(room);
				}

				roomId = room.getId();

				messages = messageRepo.findByChatRoomIdOrderByTimestampAsc(roomId);
			}
		}

		model.addAttribute("users", users);
		model.addAttribute("selectedUser", selectedUser);
		model.addAttribute("messages", messages);
		model.addAttribute("roomId", roomId);
		model.addAttribute("username", username);


		return "admin-chat";
	}
}