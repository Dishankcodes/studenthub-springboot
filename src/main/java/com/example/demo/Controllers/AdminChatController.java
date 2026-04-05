package com.example.demo.Controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.ChatUser;
import com.example.demo.entity.Student;
import com.example.demo.entity.Teacher;
import com.example.demo.entity.TeacherProfile;
import com.example.demo.enums.UserType;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.repository.ChatUserRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TeacherProfileRepo;
import com.example.demo.repository.TeacherRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminChatController {

	@Autowired
	private ChatUserRepository chatUserRepo;
	
	@Autowired
	private ChatRoomRepository chatRoomRepo;
	
	@Autowired
	private ChatMessageRepository messageRepo;
	
	@Autowired
	private StudentRepository studentRepo;
	
	@Autowired
	private TeacherRepository teacherRepo;
	
	@Autowired
	private TeacherProfileRepo teacherProfileRepo;

	private ChatUser getAdmin() {
		return chatUserRepo.findByRefIdAndType(1, UserType.ADMIN).orElseGet(() -> {
			ChatUser u = new ChatUser();
			u.setRefId(1);
			u.setType(UserType.ADMIN);
			return chatUserRepo.save(u);
		});
	}

	@GetMapping("/admin-chat")
	public String adminChat(@RequestParam(required = false) Integer userId, Model model, HttpSession session) {

		ChatUser me = getAdmin();
		session.setAttribute("chatUserId", me.getId());

		List<ChatUser> users = chatUserRepo.findAll();
		users.removeIf(u -> u.getType() == UserType.ADMIN);

		// 🔥 MAP
		Map<Integer, String> nameMap = new HashMap<>();
		Map<Integer, String> imageMap = new HashMap<>();

		for (ChatUser u : users) {

			if (u.getType() == UserType.STUDENT) {
				Student s = studentRepo.findById(u.getRefId()).orElse(null);
				if (s != null) {
					nameMap.put(u.getId(), s.getFullname());
					imageMap.put(u.getId(), s.getProfileImage());
				}
			}

			else if (u.getType() == UserType.TEACHER) {
				Teacher t = teacherRepo.findById(u.getRefId()).orElse(null);
				if (t != null) {
					nameMap.put(u.getId(), t.getFirstname() + " " + t.getLastname());

					TeacherProfile p = teacherProfileRepo.findByTeacherTeacherId(u.getRefId());
					if (p != null)
						imageMap.put(u.getId(), p.getProfileImage());
				}
			}
		}

		model.addAttribute("nameMap", nameMap);
		model.addAttribute("imageMap", imageMap);

		ChatUser selectedUser = null;
		List<ChatMessage> messages = new ArrayList<>();
		Integer roomId = null;

		String selectedName = null;
		String selectedImage = null;

		if (userId != null) {

		    ChatUser tempUser = chatUserRepo.findById(userId).orElse(null);

		    if (tempUser != null) {

		        selectedUser = tempUser; // ✅ assign once

		        // 🔥 FIXED ROOM LOGIC (NO LAMBDA)
		        ChatRoom room;
		        Optional<ChatRoom> roomOpt = chatRoomRepo.findChatRoom(me.getId(), selectedUser.getId());

		        if (roomOpt.isPresent()) {
		            room = roomOpt.get();
		        } else {
		            room = new ChatRoom();
		            room.setUser1(me);
		            room.setUser2(selectedUser);
		            room = chatRoomRepo.save(room);
		        }

		        roomId = room.getId();
		        messages = messageRepo.findByChatRoomIdOrderByTimestampAsc(roomId);

		        selectedName = nameMap.get(selectedUser.getId());
		        selectedImage = imageMap.get(selectedUser.getId());
		    }
		}
		
		Map<Integer, String> lastMessageMap = new HashMap<>();
		Map<Integer, String> timeMap = new HashMap<>();

		for (ChatUser u : users) {

		    Optional<ChatRoom> roomOpt = chatRoomRepo.findChatRoom(me.getId(), u.getId());

		    if (roomOpt.isPresent()) {

		        ChatMessage m = messageRepo
		                .findTop1ByChatRoomIdOrderByTimestampDesc(roomOpt.get().getId());

		        if (m != null) {
		            lastMessageMap.put(u.getId(), m.getContent());

		            timeMap.put(u.getId(),
		                    m.getTimestamp().toLocalTime().toString().substring(0, 5));
		        } else {
		            lastMessageMap.put(u.getId(), "Start chat...");
		            timeMap.put(u.getId(), "");
		        }

		    } else {
		        lastMessageMap.put(u.getId(), "Start chat...");
		        timeMap.put(u.getId(), "");
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

		        Long count = messageRepo.countByChatRoomIdAndSeenFalseAndSenderIdNot(
		                roomOpt.get().getId(),
		                me.getId()
		        );

		        unreadMap.put(u.getId(), count);

		    } else {
		        unreadMap.put(u.getId(), 0L);
		    }
		}

		model.addAttribute("unreadMap", unreadMap);
		model.addAttribute("lastMessageMap", lastMessageMap);
		model.addAttribute("timeMap", timeMap);
		model.addAttribute("users", users);
		model.addAttribute("selectedUser", selectedUser);
		model.addAttribute("selectedName", selectedName);
		model.addAttribute("selectedImage", selectedImage);
		model.addAttribute("messages", messages);
		model.addAttribute("roomId", roomId);

		return "admin-chat";
	}

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