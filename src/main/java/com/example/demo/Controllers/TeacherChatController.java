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
public class TeacherChatController {

	@Autowired
	private ChatUserRepository chatUserRepo;

	@Autowired
	private ChatRoomRepository chatRoomRepo;

	@Autowired
	private ChatMessageRepository messageRepo;

	@Autowired
	private EnrollmentRepository enrollmentRepo;

	@Autowired
	private TeacherRepository teacherRepo;

	// ===== CHAT PAGE =====
	@GetMapping("/teacher-chat")
	public String teacherChat(@RequestParam(required = false) Integer userId, Model model, HttpSession session) {

		Integer teacherId = 2;
//		Integer teacherId = (Integer) session.getAttribute("teacherId");
		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);

		if (teacher == null)
			return "redirect:/teacher-login";

		ChatUser me = getOrCreateTeacher(teacherId);
		session.setAttribute("chatUserId", me.getId());

		List<ChatUser> users = new ArrayList<>();

		enrollmentRepo.findByTeacherId(teacherId).forEach(e -> {
			ChatUser u = chatUserRepo.findByRefIdAndType(e.getStudent().getStudid(), UserType.STUDENT).orElseGet(() -> {
				ChatUser newUser = new ChatUser();
				newUser.setRefId(e.getStudent().getStudid());
				newUser.setType(UserType.STUDENT);
				newUser.setName(e.getStudent().getFullname());
				return chatUserRepo.save(newUser);
			});
			if (u != null && u.getId() != null)
				users.add(u);
		});

		// add admin
		ChatUser admin = chatUserRepo.findByRefIdAndType(1, UserType.ADMIN).orElse(null);
		if (admin != null)
			users.add(admin);

		users.forEach(u -> {
			if (u.getName() == null)
				u.setName("Unknown");
		});

		ChatUser selectedUser = null;
		List<ChatMessage> messages = new ArrayList<>();
		Integer roomId = null;

		if (userId != null) {

			selectedUser = chatUserRepo.findById(userId).orElse(null);

			if (selectedUser != null) {

				ChatRoom room = getRoom(me, selectedUser);
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

				long count = messageRepo.countByChatRoomIdAndSeenFalseAndSenderIdNot(rId, me.getId());

				unreadMap.put(u.getId(), count);
			} else {
				unreadMap.put(u.getId(), 0L);
			}
		}

		Map<Integer, String> lastMessageMap = new HashMap<>();

		for (ChatUser u : users) {

		    Optional<ChatRoom> roomOpt =
		            chatRoomRepo.findChatRoom(me.getId(), u.getId());

		    if (roomOpt.isPresent()) {

		        List<ChatMessage> msgs =
		            messageRepo.findTop1ByChatRoomIdOrderByTimestampDesc(
		                roomOpt.get().getId()
		            );

		        if (!msgs.isEmpty()) {
		            lastMessageMap.put(u.getId(), msgs.get(0).getContent());
		        } else {
		            lastMessageMap.put(u.getId(), "Start chat...");
		        }

		    } else {
		        lastMessageMap.put(u.getId(), "Start chat...");
		    }
		}

		model.addAttribute("lastMessageMap", lastMessageMap);
		model.addAttribute("unreadMap", unreadMap);
		model.addAttribute("teacher", teacher);
		model.addAttribute("users", users);
		model.addAttribute("selectedUser", selectedUser);
		model.addAttribute("messages", messages);
		model.addAttribute("roomId", roomId);

		return "teacher-chat";
	}

	// ===== SEND MESSAGE =====
	@PostMapping("/teacher/send")
	public String sendTeacher(@RequestParam Integer roomId, @RequestParam String content, HttpSession session) {

		Integer senderId = (Integer) session.getAttribute("chatUserId");

		ChatUser sender = chatUserRepo.findById(senderId).orElse(null);
		ChatRoom room = chatRoomRepo.findById(roomId).orElse(null);

		if (sender == null || room == null)
			return "redirect:/teacher-chat";

		ChatMessage msg = new ChatMessage();
		msg.setChatRoom(room);
		msg.setSender(sender);
		msg.setContent(content);
		msg.setTimestamp(java.time.LocalDateTime.now());

		messageRepo.save(msg);

		Integer otherId = room.getUser1().getId().equals(senderId) ? room.getUser2().getId() : room.getUser1().getId();

		return "redirect:/teacher-chat?userId=" + otherId;
	}

	private ChatUser getOrCreateTeacher(Integer id) {

		return chatUserRepo.findByRefIdAndType(id, UserType.TEACHER).orElseGet(() -> {

			Teacher t = teacherRepo.findById(id).orElse(null);

			ChatUser u = new ChatUser();
			u.setRefId(id);
			u.setType(UserType.TEACHER);

			if (t != null) {
				u.setName(t.getFirstname() + " " + t.getLastname());
			} else {
				u.setName("Unknown Teacher"); 
			}

			return chatUserRepo.save(u);
		});
	}

	private ChatRoom getRoom(ChatUser a, ChatUser b) {
		return chatRoomRepo.findChatRoom(a.getId(), b.getId()).orElseGet(() -> {
			ChatRoom r = new ChatRoom();
			r.setUser1(a);
			r.setUser2(b);
			return chatRoomRepo.save(r);
		});
	}
}