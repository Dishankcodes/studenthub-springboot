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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.ChatUser;
import com.example.demo.entity.Connection;
import com.example.demo.entity.Enrollment;
import com.example.demo.entity.Student;
import com.example.demo.entity.Teacher;
import com.example.demo.entity.TeacherProfile;
import com.example.demo.enums.ConnectionStatus;
import com.example.demo.enums.UserType;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.repository.ChatUserRepository;
import com.example.demo.repository.ConnectionRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TeacherProfileRepo;
import com.example.demo.repository.TeacherRepository;

import jakarta.mail.Message;
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

	@Autowired
	private StudentRepository studentRepo;
	@Autowired
	private TeacherRepository teacherRepo;
	@Autowired
	private TeacherProfileRepo teacherProfileRepo;

	// ================= CHAT PAGE =================
	@GetMapping("/student-chat")
	public String studentChat(@RequestParam(required = false) Integer userId, HttpSession session, Model model) {

		Integer studentId = (Integer) session.getAttribute("studentId");
		if (studentId == null)
			return "redirect:/student-login";

		ChatUser me = getOrCreate(studentId, UserType.STUDENT);

		// ===== SIDEBAR =====
		List<ChatUser> users = new ArrayList<>();

		// enrolled teachers
		List<Enrollment> enrollments = enrollmentRepo.findByStudentStudid(studentId);

		for (Enrollment e : enrollments) {
			ChatUser t = getOrCreate(e.getCourse().getTeacher().getTeacherId(), UserType.TEACHER);
			users.add(t);
		}

		// connections
		List<Connection> connections = connectionRepo.findBySenderIdAndStatusOrReceiverIdAndStatus(me.getId(),
				ConnectionStatus.ACCEPTED, me.getId(), ConnectionStatus.ACCEPTED);

		for (Connection c : connections) {
			ChatUser other = c.getSender().getId().equals(me.getId()) ? c.getReceiver() : c.getSender();

			users.add(other);
		}

		model.addAttribute("users", users);

		// ===== CHAT =====
		ChatUser selectedUser = null;
		List<ChatMessage> messages = new ArrayList<>();
		Integer roomId = null;
		boolean canChat = false;

		if (userId != null) {

			Optional<ChatUser> optionalUser = chatUserRepo.findById(userId);

			if (optionalUser.isPresent()) {

				selectedUser = optionalUser.get();
				canChat = isAllowed(me, selectedUser);

				if (canChat) {

					ChatRoom room = getRoom(me, selectedUser);
					roomId = room.getId();

					messages = messageRepo.findByChatRoomIdOrderByTimestampAsc(roomId);
				}
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

		model.addAttribute("unreadMap", unreadMap);
		session.setAttribute("chatUserId", me.getId());
		model.addAttribute("selectedUser", selectedUser);
		model.addAttribute("messages", messages);
		model.addAttribute("roomId", roomId);
		model.addAttribute("canChat", canChat);

		return "student-chat";
	}

	// ================= SEND MESSAGE =================
	@PostMapping("/student/send")
	public String send(@RequestParam Integer roomId, @RequestParam String content, HttpSession session) {

		 Integer studentId = (Integer) session.getAttribute("studentId");

		    if (studentId == null) {
		        return "redirect:/student-login";
		    }

		ChatUser sender = getOrCreate(studentId, UserType.STUDENT);
		ChatRoom room = chatRoomRepo.findById(roomId).orElseThrow();

		ChatMessage msg = new ChatMessage();
		msg.setChatRoom(room);
		msg.setSender(sender);
		msg.setContent(content);
		msg.setTimestamp(LocalDateTime.now());

		messageRepo.save(msg);

		Integer otherId = room.getUser1().getId().equals(sender.getId()) ? room.getUser2().getId()
				: room.getUser1().getId();

		return "redirect:/student-chat?userId=" + otherId;
	}

	// ================= FOLLOW =================
	@PostMapping("/chat/follow")
	public String follow(@RequestParam Integer receiverId, HttpSession session) {

		 Integer studentId = (Integer) session.getAttribute("studentId");

		    if (studentId == null) {
		        return "redirect:/student-login";
		    }

		ChatUser sender = getOrCreate(studentId, UserType.STUDENT);
		ChatUser receiver = chatUserRepo.findById(receiverId).orElseThrow();

		Connection conn = new Connection();
		conn.setSender(sender);
		conn.setReceiver(receiver);
		conn.setStatus(ConnectionStatus.PENDING);

		connectionRepo.save(conn);

		return "redirect:/student-chat";
	}

	// ================= NOTIFICATIONS =================
	@GetMapping("/student-notifications")
	public String notifications(HttpSession session, Model model) {

		 Integer studentId = (Integer) session.getAttribute("studentId");

		    if (studentId == null) {
		        return "redirect:/student-login";
		    }

		ChatUser me = getOrCreate(studentId, UserType.STUDENT);

		List<Connection> requests = connectionRepo.findByReceiverIdAndStatus(me.getId(), ConnectionStatus.PENDING);

		model.addAttribute("requests", requests);

		return "student-notifications";
	}

	// ================= ACCEPT =================
	@PostMapping("/chat/accept")
	public String accept(@RequestParam Integer id) {

		Connection c = connectionRepo.findById(id).orElseThrow();
		c.setStatus(ConnectionStatus.ACCEPTED);
		connectionRepo.save(c);

		return "redirect:/student-notifications";
	}

	// ================= REJECT =================
	@PostMapping("/chat/reject")
	public String reject(@RequestParam Integer id) {

		connectionRepo.deleteById(id);
		return "redirect:/student-notifications";
	}

	// ================= SEARCH =================
	@GetMapping("/student-search")
	public String searchUsers(@RequestParam(required = false) String keyword, HttpSession session, Model model) {

		 Integer studentId = (Integer) session.getAttribute("studentId");

		    if (studentId == null) {
		        return "redirect:/student-login";
		    }

		ChatUser me = getOrCreate(studentId, UserType.STUDENT);

		List<Map<String, Object>> result = new ArrayList<>();

		if (keyword != null && !keyword.isEmpty()) {

			List<ChatUser> users = chatUserRepo.findByNameContainingIgnoreCase(keyword);

			for (ChatUser u : users) {

				if (u.getId().equals(me.getId()))
					continue;

				Map<String, Object> map = new HashMap<>();
				map.put("user", u);
				map.put("status", getStatus(me, u));

				result.add(map);
			}
		}

		model.addAttribute("results", result);

		return "student-search";
	}

	// ================= HELPERS =================

	private ChatUser getOrCreate(Integer refId, UserType type) {

		return chatUserRepo.findByRefIdAndType(refId, type).orElseGet(() -> {

			ChatUser u = new ChatUser();
			u.setRefId(refId);
			u.setType(type);

			if (type == UserType.STUDENT) {
				Student s = studentRepo.findById(refId).orElse(null);
				if (s != null) {
					u.setName(s.getFullname());
					u.setProfileImage(s.getProfileImage());
				}
			}

			if (type == UserType.TEACHER) {
				Teacher t = teacherRepo.findById(refId).orElse(null);
				if (t != null) {
					u.setName(t.getFirstname() + " " + t.getLastname());
					TeacherProfile p = teacherProfileRepo.findByTeacherTeacherId(refId);
					if (p != null)
						u.setProfileImage(p.getProfileImage());
				}
			}

			if (type == UserType.ADMIN) {
				u.setName("Admin");
			}

			return chatUserRepo.save(u);
		});
	}

	private ChatRoom getRoom(ChatUser a, ChatUser b) {

		Optional<ChatRoom> r = chatRoomRepo.findChatRoom(a.getId(), b.getId());

		if (r.isPresent())
			return r.get();

		ChatRoom room = new ChatRoom();
		room.setUser1(a);
		room.setUser2(b);

		return chatRoomRepo.save(room);
	}

	private boolean isAllowed(ChatUser me, ChatUser other) {

		if (other.getType() == UserType.TEACHER)
			return true;

		return connectionRepo.findBySenderIdAndReceiverIdOrSenderIdAndReceiverId(me.getId(), other.getId(),
				other.getId(), me.getId()).map(c -> c.getStatus() == ConnectionStatus.ACCEPTED).orElse(false);
	}

	private String getStatus(ChatUser me, ChatUser other) {

		Optional<Connection> c = connectionRepo.findBySenderIdAndReceiverIdOrSenderIdAndReceiverId(me.getId(),
				other.getId(), other.getId(), me.getId());

		if (c.isEmpty())
			return "FOLLOW";
		if (c.get().getStatus() == ConnectionStatus.PENDING)
			return "PENDING";

		return "CHAT";
	}
}