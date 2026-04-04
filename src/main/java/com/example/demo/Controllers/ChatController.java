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

	@GetMapping("/student-chat")
	public String studentChat(@RequestParam(required = false) Integer userId, HttpSession session, Model model) {

		Integer studentId = (Integer) session.getAttribute("studentId");
		if (studentId == null)
			return "redirect:/student-login";

		ChatUser me = getOrCreate(studentId, UserType.STUDENT);

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

		// add admin
		ChatUser admin = chatUserRepo.findByRefIdAndType(1, UserType.ADMIN).orElse(null);
		if (admin != null)
			users.add(admin);

		// 🔥 NAME + IMAGE MAP
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

			else if (u.getType() == UserType.ADMIN) {
				nameMap.put(u.getId(), "Support Admin");
				imageMap.put(u.getId(), "/images/admin-avatar.png");
			}
		}

		model.addAttribute("nameMap", nameMap);
		model.addAttribute("imageMap", imageMap);

		ChatUser selectedUser = null;
		List<ChatMessage> messages = new ArrayList<>();
		Integer roomId = null;
		boolean canChat = false;

		String selectedName = null;
		String selectedImage = null;

		if (userId != null) {

			selectedUser = chatUserRepo.findById(userId).orElse(null);

			if (selectedUser != null) {

				canChat = isAllowed(me, selectedUser);

				if (canChat) {
					ChatRoom room = getRoom(me, selectedUser);
					roomId = room.getId();
					messages = messageRepo.findByChatRoomIdOrderByTimestampAsc(roomId);
				}

				selectedName = nameMap.get(selectedUser.getId());
				selectedImage = imageMap.get(selectedUser.getId());
			}
		}

		model.addAttribute("selectedUser", selectedUser);
		model.addAttribute("selectedName", selectedName);
		model.addAttribute("selectedImage", selectedImage);
		model.addAttribute("messages", messages);
		model.addAttribute("roomId", roomId);
		model.addAttribute("canChat", canChat);

		Map<Integer, Long> unreadMap = new HashMap<>();

		for (ChatUser u : users) {
			Optional<ChatRoom> roomOpt = chatRoomRepo.findChatRoom(me.getId(), u.getId());

			if (roomOpt.isPresent()) {
				long count = messageRepo.countByChatRoomIdAndSeenFalseAndSenderIdNot(roomOpt.get().getId(), me.getId());
				unreadMap.put(u.getId(), count);
			} else {
				unreadMap.put(u.getId(), 0L);
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

		model.addAttribute("lastMessageMap", lastMessageMap);
		model.addAttribute("timeMap", timeMap);

		model.addAttribute("unreadMap", unreadMap);
		model.addAttribute("users", users);

		session.setAttribute("chatUserId", me.getId());

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

	// ================= HELPERS =================
	private ChatUser getOrCreate(Integer refId, UserType type) {

		return chatUserRepo.findByRefIdAndType(refId, type).orElseGet(() -> {
			ChatUser u = new ChatUser();
			u.setRefId(refId);
			u.setType(type);
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

		// ✅ ALLOW ADMIN CHAT ALWAYS
		if (other.getType() == UserType.ADMIN) {
			return true;
		}

		// ✅ STUDENT → TEACHER (only if enrolled)
		if (other.getType() == UserType.TEACHER) {
			return enrollmentRepo.existsByStudentStudidAndCourseCourseId(me.getRefId(), other.getRefId());
		}

		// ✅ STUDENT ↔ STUDENT (only if accepted)
		return connectionRepo.findBySenderIdAndReceiverIdOrSenderIdAndReceiverId(me.getId(), other.getId(),
				other.getId(), me.getId()).map(c -> c.getStatus() == ConnectionStatus.ACCEPTED).orElse(false);
	}
//
//	private String getStatus(ChatUser me, ChatUser other) {
//
//		Optional<Connection> c = connectionRepo.findBySenderIdAndReceiverIdOrSenderIdAndReceiverId(me.getId(),
//				other.getId(), other.getId(), me.getId());
//
//		if (c.isEmpty())
//			return "FOLLOW";
//		if (c.get().getStatus() == ConnectionStatus.PENDING)
//			return "PENDING";
//
//		return "CHAT";
//	}
}