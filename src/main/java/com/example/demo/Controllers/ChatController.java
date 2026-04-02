package com.example.demo.Controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.ChatUser;
import com.example.demo.entity.Connection;
import com.example.demo.entity.Enrollment;
import com.example.demo.enums.ConnectionStatus;
import com.example.demo.enums.UserType;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.repository.ChatUserRepository;
import com.example.demo.repository.ConnectionRepository;
import com.example.demo.repository.EnrollmentRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class ChatController {

	@Autowired
	private ChatUserRepository chatUserRepo;
	
	@Autowired
	private ChatRoomRepository chatRoomRepo;
	
	@Autowired
	private ChatMessageRepository messageRepo;
	
	@Autowired
	private ConnectionRepository connectionRepo;
	
	@Autowired
	private EnrollmentRepository enrollmentRepo;

	@GetMapping("/student-chat")
	public String studentChat(HttpSession session, Model model) {

		Integer studentId = (Integer) session.getAttribute("studentId");
		if (studentId == null)
			return "redirect:/student-login";

		ChatUser me = getOrCreate(studentId, UserType.STUDENT);

		List<ChatUser> users = getStudentSidebarUsers(me);

		model.addAttribute("users", users);
		model.addAttribute("me", me);

		return "student-chat";
	}

	
	@GetMapping("/student-search")
	public String searchUsers(@RequestParam String keyword, Model model, HttpSession session) {

		Integer studentId = (Integer) session.getAttribute("studentId");
		ChatUser me = getOrCreate(studentId, UserType.STUDENT);

		List<ChatUser> users = chatUserRepo.findByNameContainingIgnoreCase(keyword);

		List<Map<String, Object>> result = new ArrayList<>();

		for (ChatUser u : users) {

			if (u.getId().equals(me.getId()))
				continue;

			Map<String, Object> map = new HashMap<>();
			map.put("user", u);
			map.put("status", getConnectionStatus(me, u));

			result.add(map);
		}

		model.addAttribute("results", result);

		return "student-search";
	}

	@PostMapping("/chat/follow")
	public String follow(@RequestParam Integer receiverId, HttpSession session) {

		Integer studentId = (Integer) session.getAttribute("studentId");

		ChatUser sender = getOrCreate(studentId, UserType.STUDENT);
		ChatUser receiver = chatUserRepo.findById(receiverId).orElseThrow();

		Connection conn = new Connection();
		conn.setSender(sender);
		conn.setReceiver(receiver);
		conn.setStatus(ConnectionStatus.PENDING);

		connectionRepo.save(conn);

		return "redirect:/student-search?keyword=";
	}

	@PostMapping("/chat/accept")
	public String accept(@RequestParam Integer connectionId) {

		Connection conn = connectionRepo.findById(connectionId).orElseThrow();
		conn.setStatus(ConnectionStatus.ACCEPTED);
		connectionRepo.save(conn);

		return "redirect:/student-chat";
	}

	// =========================
	// ❌ REJECT REQUEST
	// =========================
	@PostMapping("/chat/reject")
	public String reject(@RequestParam Integer connectionId) {

		connectionRepo.deleteById(connectionId);
		return "redirect:/student-chat";
	}

	// =========================
	// 💬 START CHAT
	// =========================
	@GetMapping("/chat/start")
	public String startChat(@RequestParam Integer receiverId, HttpSession session) {

		Integer studentId = (Integer) session.getAttribute("studentId");

		ChatUser sender = getOrCreate(studentId, UserType.STUDENT);
		ChatUser receiver = chatUserRepo.findById(receiverId).orElseThrow();

		// 🔒 must be connected
		if (!isConnected(sender, receiver)) {
			return "redirect:/student-chat?error=not-connected";
		}

		Optional<ChatRoom> room = chatRoomRepo.findChatRoom(sender.getId(), receiver.getId());

		if (room.isPresent())
			return "redirect:/chat/" + room.get().getId();

		ChatRoom r = new ChatRoom();
		r.setUser1(sender);
		r.setUser2(receiver);

		ChatRoom saved = chatRoomRepo.save(r);

		return "redirect:/chat/" + saved.getId();
	}

	// =========================
	// 💬 OPEN CHAT
	// =========================
	@GetMapping("/chat/{roomId}")
	public String openChat(@PathVariable Integer roomId, Model model) {

		List<ChatMessage> messages = messageRepo.findByChatRoomIdOrderByTimestampAsc(roomId);

		model.addAttribute("messages", messages);
		model.addAttribute("roomId", roomId);

		return "student-chat";
	}

	// =========================
	// 📤 SEND MESSAGE
	// =========================
	@PostMapping("/chat/send")
	public String send(@RequestParam Integer roomId, @RequestParam String content, HttpSession session) {

		Integer studentId = (Integer) session.getAttribute("studentId");

		ChatUser sender = getOrCreate(studentId, UserType.STUDENT);
		ChatRoom room = chatRoomRepo.findById(roomId).orElseThrow();

		ChatMessage msg = new ChatMessage();
		msg.setChatRoom(room);
		msg.setSender(sender);
		msg.setContent(content);
		msg.setTimestamp(LocalDateTime.now());

		messageRepo.save(msg);

		return "redirect:/chat/" + roomId;
	}

	// =========================
	// 🔧 HELPERS
	// =========================
	private ChatUser getOrCreate(Integer refId, UserType type) {
		return chatUserRepo.findByRefIdAndType(refId, type).orElseGet(() -> {
			ChatUser u = new ChatUser();
			u.setRefId(refId);
			u.setType(type);
			u.setName("User " + refId);
			return chatUserRepo.save(u);
		});
	}

	private boolean isConnected(ChatUser a, ChatUser b) {
		return connectionRepo
				.findBySenderIdAndReceiverIdOrSenderIdAndReceiverId(a.getId(), b.getId(), b.getId(), a.getId())
				.map(c -> c.getStatus() == ConnectionStatus.ACCEPTED).orElse(false);
	}

	private String getConnectionStatus(ChatUser me, ChatUser other) {

		Optional<Connection> conn = connectionRepo.findBySenderIdAndReceiverIdOrSenderIdAndReceiverId(me.getId(),
				other.getId(), other.getId(), me.getId());

		if (conn.isEmpty())
			return "FOLLOW";

		Connection c = conn.get();

		if (c.getStatus() == ConnectionStatus.ACCEPTED)
			return "CHAT";
		if (c.getStatus() == ConnectionStatus.PENDING)
			return "PENDING";

		return "FOLLOW";
	}

	private List<ChatUser> getStudentSidebarUsers(ChatUser me) {

		List<ChatUser> list = new ArrayList<>();

		// teachers (enrolled)
		List<Enrollment> enrollments = enrollmentRepo.findByStudentStudid(me.getRefId());

		for (Enrollment e : enrollments) {
			chatUserRepo.findByRefIdAndType(e.getCourse().getTeacher().getTeacherId(), UserType.TEACHER)
					.ifPresent(list::add);
		}

		// connections
		List<Connection> connections = connectionRepo.findBySenderIdAndStatusOrReceiverIdAndStatus(me.getId(),
				ConnectionStatus.ACCEPTED, me.getId(), ConnectionStatus.ACCEPTED);

		for (Connection c : connections) {
			ChatUser other = c.getSender().getId().equals(me.getId()) ? c.getReceiver() : c.getSender();

			list.add(other);
		}

		return list;
	}
}