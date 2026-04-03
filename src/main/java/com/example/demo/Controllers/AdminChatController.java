package com.example.demo.Controllers;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.*;
import com.example.demo.enums.UserType;
import com.example.demo.repository.*;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminChatController {

	@Autowired
	private ChatUserRepository chatUserRepo;
	@Autowired
	private ChatRoomRepository chatRoomRepo;
	@Autowired
	private ChatMessageRepository messageRepo;

	private ChatUser getAdmin() {
		return chatUserRepo.findByRefIdAndType(1, UserType.ADMIN).orElseGet(() -> {
			ChatUser u = new ChatUser();
			u.setRefId(1);
			u.setType(UserType.ADMIN);
			u.setName("Admin");
			return chatUserRepo.save(u);
		});
	}

	@GetMapping("/admin-chat")
	public String adminChat(@RequestParam(required = false) Integer userId, Model model, HttpSession session) {

		ChatUser me = getAdmin();
		session.setAttribute("chatUserId", me.getId());

		List<ChatUser> users = chatUserRepo.findAll();
		users.removeIf(u -> u.getType() == UserType.ADMIN);

		users.forEach(u -> {
			if (u.getName() == null)
				u.setName("Unknown");
		});

		ChatUser selectedUser = null;
		List<ChatMessage> messages = new ArrayList<>();
		Integer roomId = null;

		if (userId != null) {

			ChatUser tempUser = chatUserRepo.findById(userId).orElse(null);

			if (tempUser != null) {

				selectedUser = tempUser;
				
				ChatRoom room = chatRoomRepo.findChatRoom(me.getId(), tempUser.getId()).orElseGet(() -> {
					ChatRoom r = new ChatRoom();
					r.setUser1(me);
					r.setUser2(tempUser);
					return chatRoomRepo.save(r);
				});

				roomId = room.getId();
				messages = messageRepo.findByChatRoomIdOrderByTimestampAsc(roomId);
			}
		}

		if (roomId != null) {
		    for (ChatMessage msg : messages) {
		        if (!msg.isSeen() && !msg.getSender().getId().equals(me.getId())) {
		            msg.setSeen(true);
		        }
		    }
		    messageRepo.saveAll(messages);
		}
		
		Map<Integer, Long> unreadMap = new HashMap<>();

		for (ChatUser u : users) {

		    Optional<ChatRoom> roomOpt = chatRoomRepo.findChatRoom(me.getId(), u.getId());

		    if (roomOpt.isPresent()) {
		        Integer rId = roomOpt.get().getId();

		        long count = messageRepo.countByChatRoomIdAndSeenFalseAndSenderIdNot(
		                rId, me.getId()
		        );

		        unreadMap.put(u.getId(), count);
		    } else {
		        unreadMap.put(u.getId(), 0L);
		    }
		}

		model.addAttribute("unreadMap", unreadMap);
		model.addAttribute("users", users);
		model.addAttribute("selectedUser", selectedUser);
		model.addAttribute("messages", messages);
		model.addAttribute("roomId", roomId);

		return "admin-chat";
	}

	// ===== SEND =====
	@PostMapping("/admin/send")
	public String sendAdmin(@RequestParam Integer roomId, @RequestParam String content, HttpSession session) {

		Integer senderId = (Integer) session.getAttribute("chatUserId");

		ChatUser sender = chatUserRepo.findById(senderId).orElse(null);
		ChatRoom room = chatRoomRepo.findById(roomId).orElse(null);

		if (sender == null || room == null)
			return "redirect:/admin-chat";

		ChatMessage msg = new ChatMessage();
		msg.setChatRoom(room);
		msg.setSender(sender);
		msg.setContent(content);
		msg.setTimestamp(java.time.LocalDateTime.now());

		messageRepo.save(msg);

		Integer otherId = room.getUser1().getId().equals(senderId) ? room.getUser2().getId() : room.getUser1().getId();

		return "redirect:/admin-chat?userId=" + otherId;
	}
}