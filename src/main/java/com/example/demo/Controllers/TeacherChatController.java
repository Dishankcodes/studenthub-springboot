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
import com.example.demo.enums.UserType;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.repository.ChatUserRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TeacherRepository;

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

	@Autowired
	private StudentRepository studentRepo;

	@GetMapping("/teacher-chat")
	public String teacherChat(@RequestParam(required = false) Integer userId, Model model, HttpSession session) {
		Integer teacherId = (Integer) session.getAttribute("teacherId");
		if (teacherId == null)
			return "redirect:/teacher-auth";

		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
		if (teacher == null) {
			session.invalidate();
			return "redirect:/teacher-auth";
		}

		ChatUser me = getOrCreateTeacher(teacherId);
		session.setAttribute("chatUserId", me.getId());

		List<ChatUser> users = new ArrayList<>();

		// ================= ✅ 1. ENROLLED STUDENTS =================
		for (var e : enrollmentRepo.findByTeacherId(teacherId)) {

			ChatUser u = chatUserRepo.findByRefIdAndType(e.getStudent().getStudid(), UserType.STUDENT).orElseGet(() -> {
				ChatUser newUser = new ChatUser();
				newUser.setRefId(e.getStudent().getStudid());
				newUser.setType(UserType.STUDENT);
				return chatUserRepo.save(newUser);
			});

			users.add(u);
		}

		// ================= ✅ 2. ADMIN =================
		ChatUser admin = chatUserRepo.findByRefIdAndType(1, UserType.ADMIN).orElse(null);
		if (admin != null)
			users.add(admin);

		// ================= ✅ 3. ADD CHAT USERS (IMPORTANT FIX) =================
		List<ChatRoom> rooms = chatRoomRepo.findByUser1IdOrUser2Id(me.getId(), me.getId());

		for (ChatRoom r : rooms) {

			ChatUser other = r.getUser1().getId().equals(me.getId()) ? r.getUser2() : r.getUser1();

			users.add(other);
		}

		// ================= ✅ REMOVE DUPLICATES =================
		users = new ArrayList<>(new java.util.LinkedHashSet<>(users));

		// ================= ✅ 4. NAME + IMAGE MAP (FIXED ORDER) =================
		Map<Integer, String> nameMap = new HashMap<>();
		Map<Integer, String> imageMap = new HashMap<>();

		for (ChatUser u : users) {

			if (u.getType() == UserType.STUDENT) {

				Student s = studentRepo.findById(u.getRefId()).orElse(null);

				if (s != null) {
					nameMap.put(u.getId(), s.getFullname());
					imageMap.put(u.getId(), s.getProfileImage());
				} else {
					nameMap.put(u.getId(), "Student");
					imageMap.put(u.getId(), "/images/default.jpg");
				}
			}

			else if (u.getType() == UserType.TEACHER) {

				Teacher t = teacherRepo.findById(u.getRefId()).orElse(null);

				if (t != null) {
					nameMap.put(u.getId(), t.getFirstname() + " " + t.getLastname());
				} else {
					nameMap.put(u.getId(), "Teacher");
				}
			}

			else if (u.getType() == UserType.ADMIN) {
				nameMap.put(u.getId(), "Support Admin");
				imageMap.put(u.getId(), "/images/admin-avatar.png");
			}
		}

		// ================= ✅ 5. SELECTED USER =================
		ChatUser selectedUser = null;
		List<ChatMessage> messages = new ArrayList<>();
		Integer roomId = null;

		String selectedName = null;
		String selectedImage = null;

		if (userId != null) {

			selectedUser = chatUserRepo.findById(userId).orElse(null);

			if (selectedUser != null) {

				ChatRoom room = getRoom(me, selectedUser);
				roomId = room.getId();

				messages = messageRepo.findByChatRoomIdOrderByTimestampAsc(roomId);

				selectedName = nameMap.get(selectedUser.getId());
				selectedImage = imageMap.get(selectedUser.getId());
			}
		}

		// ================= ✅ 6. MARK SEEN =================
		if (roomId != null) {
			for (ChatMessage msg : messages) {
				if (!msg.isSeen() && !msg.getSender().getId().equals(me.getId())) {
					msg.setSeen(true);
				}
			}
			messageRepo.saveAll(messages);
		}

		// ================= ✅ 7. UNREAD =================
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

				ChatMessage m = messageRepo.findTop1ByChatRoomIdOrderByTimestampDesc(roomOpt.get().getId());

				if (m != null) {
					lastMessageMap.put(u.getId(), m.getContent());

					timeMap.put(u.getId(), m.getTimestamp().toLocalTime().toString().substring(0, 5));
				} else {
					lastMessageMap.put(u.getId(), "Start chat...");
					timeMap.put(u.getId(), "");
				}

			} else {
				lastMessageMap.put(u.getId(), "Start chat...");
				timeMap.put(u.getId(), "");
			}
		}
		if (userId != null) {

			selectedUser = chatUserRepo.findById(userId).orElse(null);

			if (selectedUser == null) {
				return "redirect:/teacher-chat";
			}
		}

		model.addAttribute("nameMap", nameMap);
		model.addAttribute("imageMap", imageMap);
		model.addAttribute("lastMessageMap", lastMessageMap);
		model.addAttribute("timeMap", timeMap);
		model.addAttribute("unreadMap", unreadMap);
		model.addAttribute("teacher", teacher);
		model.addAttribute("users", users);
		model.addAttribute("selectedUser", selectedUser);
		model.addAttribute("selectedName", selectedName);
		model.addAttribute("selectedImage", selectedImage);
		model.addAttribute("messages", messages);
		model.addAttribute("roomId", roomId);
		model.addAttribute("teacher", teacher);

		return "teacher-chat";
	}

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
			ChatUser u = new ChatUser();
			u.setRefId(id);
			u.setType(UserType.TEACHER);
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