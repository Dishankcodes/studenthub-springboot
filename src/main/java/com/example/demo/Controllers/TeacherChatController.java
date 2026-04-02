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
import com.example.demo.entity.Enrollment;
import com.example.demo.entity.Teacher;
import com.example.demo.enums.UserType;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.repository.ChatUserRepository;
import com.example.demo.repository.EnrollmentRepository;
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

	@GetMapping("/teacher-chat")
	public String teacherChat(@RequestParam(required = false) Integer userId, HttpSession session, Model model) {

		Integer teacherId = (Integer) session.getAttribute("teacherId");
		
		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
		if (teacher == null) {
			session.invalidate();
			return "redirect:/teacher-auth";
		}

		if (teacherId == null)
			return "redirect:/teacher-auth";

		ChatUser me = chatUserRepo.findByRefIdAndType(teacherId, UserType.TEACHER).orElseThrow();

		List<ChatUser> users = new ArrayList<>();

		List<Enrollment> enrollments = enrollmentRepo.findByTeacherId(teacherId);

		for (Enrollment e : enrollments) {

			chatUserRepo.findByRefIdAndType(e.getStudent().getStudid(), UserType.STUDENT).ifPresent(users::add);
		}

		ChatUser selectedUser = null;
		List<ChatMessage> messages = new ArrayList<>();
		Integer roomId = null;

		if (userId != null) {

			selectedUser = chatUserRepo.findById(userId).orElse(null);

			if (selectedUser != null) {

				Optional<ChatRoom> existingRoom = chatRoomRepo.findChatRoom(me.getId(), selectedUser.getId());

				ChatRoom room;

				if (existingRoom.isPresent()) {
					room = existingRoom.get();
				} else {
					room = new ChatRoom();
					room.setUser1(me);
					room.setUser2(selectedUser);
					room = chatRoomRepo.save(room);
				}

				roomId = room.getId();

				messages = messageRepo.findByChatRoomIdOrderByTimestampAsc(roomId);
			}
		}

		model.addAttribute("teacher", teacher);
		model.addAttribute("users", users);
		model.addAttribute("selectedUser", selectedUser);
		model.addAttribute("messages", messages);
		model.addAttribute("roomId", roomId);

		return "teacher-chat";
	}
}