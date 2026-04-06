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
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.ChatUser;
import com.example.demo.entity.Connection;
import com.example.demo.entity.Student;
import com.example.demo.entity.Teacher;
import com.example.demo.entity.TeacherProfile;
import com.example.demo.enums.ConnectionStatus;
import com.example.demo.enums.UserType;
import com.example.demo.repository.ChatUserRepository;
import com.example.demo.repository.ConnectionRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TeacherProfileRepo;
import com.example.demo.repository.TeacherRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class StudentConnectionController {

	@Autowired
	private ChatUserRepository chatUserRepo;

	@Autowired
	private ConnectionRepository connectionRepo;

	@Autowired
	private StudentRepository studentRepo;

	@Autowired
	private TeacherRepository teacherRepo;

	@Autowired
	private TeacherProfileRepo teacherProfileRepo;

	// ================= 🔍 SEARCH =================
	@GetMapping("/student-search")
	public String searchUsers(@RequestParam(required = false) String keyword, HttpSession session, Model model) {

		Integer studentId = (Integer) session.getAttribute("studentId");
		if (studentId == null)
			return "redirect:/student-login";

		ChatUser me = getOrCreate(studentId, UserType.STUDENT);

		List<Map<String, Object>> results = new ArrayList<>();

		if (keyword == null || keyword.trim().isEmpty()) {

			List<Student> students = studentRepo.findAll().stream().limit(5).toList();
			List<Teacher> teachers = teacherRepo.findAll().stream().limit(5).toList();

			// students
			for (Student s : students) {

				if (s.getStudid().equals(studentId))
					continue;

				ChatUser u = getOrCreate(s.getStudid(), UserType.STUDENT);

				Map<String, Object> map = new HashMap<>();
				map.put("id", u.getId());
				map.put("name", s.getFullname());
				map.put("type", "STUDENT");
				map.put("image", s.getProfileImage());
				map.put("status", getConnectionStatus(me, u));

				results.add(map);
			}

			// teachers
			for (Teacher t : teachers) {

				ChatUser u = getOrCreate(t.getTeacherId(), UserType.TEACHER);

				TeacherProfile p = teacherProfileRepo.findByTeacherTeacherId(t.getTeacherId());

				Map<String, Object> map = new HashMap<>();
				map.put("id", u.getId());
				map.put("name", t.getFirstname() + " " + t.getLastname());
				map.put("type", "TEACHER");
				map.put("image", p != null ? p.getProfileImage() : null);
				map.put("status", getConnectionStatus(me, u));

				results.add(map);
			}
		}

		// 🔍 SEARCH MODE
		else {

			List<Student> students = studentRepo.findByFullnameContainingIgnoreCase(keyword);

			for (Student s : students) {

				if (s.getStudid().equals(studentId))
					continue;

				ChatUser u = getOrCreate(s.getStudid(), UserType.STUDENT);

				Map<String, Object> map = new HashMap<>();
				map.put("id", u.getId());
				map.put("name", s.getFullname());
				map.put("type", "STUDENT");
				map.put("image", s.getProfileImage());
				map.put("status", getConnectionStatus(me, u));

				results.add(map);
			}

			List<Teacher> teachers = teacherRepo
					.findByFirstnameContainingIgnoreCaseOrLastnameContainingIgnoreCase(keyword, keyword);

			for (Teacher t : teachers) {

				ChatUser u = getOrCreate(t.getTeacherId(), UserType.TEACHER);

				TeacherProfile p = teacherProfileRepo.findByTeacherTeacherId(t.getTeacherId());

				Map<String, Object> map = new HashMap<>();
				map.put("id", u.getId());
				map.put("name", t.getFirstname() + " " + t.getLastname());
				map.put("type", "TEACHER");
				map.put("image", p != null ? p.getProfileImage() : null);
				map.put("status", getConnectionStatus(me, u));

				results.add(map);
			}
		}

		model.addAttribute("results", results);

		return "student-search";
	}

	// ================= ➕ SEND REQUEST =================
	@PostMapping("/connection/send")
	public String sendRequest(@RequestParam Integer receiverId, HttpSession session) {

		Integer studentId = (Integer) session.getAttribute("studentId");
		if (studentId == null)
			return "redirect:/student-login";

		ChatUser sender = getOrCreate(studentId, UserType.STUDENT);
		ChatUser receiver = chatUserRepo.findById(receiverId).orElseThrow();

		// prevent duplicate
		Optional<Connection> existing = connectionRepo.findBySenderIdAndReceiverId(sender.getId(), receiver.getId());

		if (existing.isPresent())
			return "redirect:/student-search";

		Connection c = new Connection();
		c.setSender(sender);
		c.setReceiver(receiver);
		c.setStatus(ConnectionStatus.PENDING);

		connectionRepo.save(c);

		return "redirect:/student-search";
	}

	// ================= 🔔 NOTIFICATIONS =================
	@GetMapping("/student-notification")
	public String notifications(HttpSession session, Model model) {

		Integer studentId = (Integer) session.getAttribute("studentId");
		if (studentId == null)
			return "redirect:/student-login";

		ChatUser me = getOrCreate(studentId, UserType.STUDENT);

		// 🔹 Incoming requests
		List<Connection> received = connectionRepo.findByReceiverIdAndStatus(me.getId(), ConnectionStatus.PENDING);

		// 🔹 Sent requests
		List<Connection> sent = connectionRepo.findBySenderIdAndStatus(me.getId(), ConnectionStatus.PENDING);

		Map<Integer, String> nameMap = new HashMap<>();
		Map<Integer, String> imageMap = new HashMap<>();

		// for received
		for (Connection c : received) {

			ChatUser u = c.getSender();

			if (u.getType() == UserType.STUDENT) {
				Student s = studentRepo.findById(u.getRefId()).orElse(null);
				if (s != null) {
					nameMap.put(u.getId(), s.getFullname());
					imageMap.put(u.getId(), s.getProfileImage());
				}
			} else {
				Teacher t = teacherRepo.findById(u.getRefId()).orElse(null);
				if (t != null) {
					nameMap.put(u.getId(), t.getFirstname() + " " + t.getLastname());

					TeacherProfile p = teacherProfileRepo.findByTeacherTeacherId(u.getRefId());
					if (p != null)
						imageMap.put(u.getId(), p.getProfileImage());
				}
			}
		}

		// for sent
		for (Connection c : sent) {

			ChatUser u = c.getReceiver();

			if (u.getType() == UserType.STUDENT) {
				Student s = studentRepo.findById(u.getRefId()).orElse(null);
				if (s != null) {
					nameMap.put(u.getId(), s.getFullname());
					imageMap.put(u.getId(), s.getProfileImage());
				}
			} else {
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
		model.addAttribute("receivedRequests", received);
		model.addAttribute("sentRequests", sent);

		return "student-notification";
	}

	@PostMapping("/connection/cancel")
	public String cancel(@RequestParam Integer id) {

		connectionRepo.deleteById(id);

		return "redirect:/student-notification";
	}

	// ================= ✅ ACCEPT =================
	@PostMapping("/connection/accept")
	public String accept(@RequestParam Integer id) {

		Connection c = connectionRepo.findById(id).orElseThrow();
		c.setStatus(ConnectionStatus.ACCEPTED);
		connectionRepo.save(c);

		return "redirect:/student-notification";
	}

	// ================= ❌ REJECT =================
	@PostMapping("/connection/reject")
	public String reject(@RequestParam Integer id) {

		connectionRepo.deleteById(id);
		return "redirect:/student-notification";
	}

	// ================= 🧠 STATUS LOGIC =================
	private String getConnectionStatus(ChatUser me, ChatUser other) {

		Optional<Connection> sent = connectionRepo.findBySenderIdAndReceiverId(me.getId(), other.getId());

		Optional<Connection> received = connectionRepo.findByReceiverIdAndSenderId(me.getId(), other.getId());

		if (sent.isPresent()) {
			if (sent.get().getStatus() == ConnectionStatus.PENDING)
				return "PENDING";
			if (sent.get().getStatus() == ConnectionStatus.ACCEPTED)
				return "CHAT";
		}

		if (received.isPresent()) {
			if (received.get().getStatus() == ConnectionStatus.PENDING)
				return "REQUEST_RECEIVED";
			if (received.get().getStatus() == ConnectionStatus.ACCEPTED)
				return "CHAT";
		}

		return "FOLLOW";
	}

	private ChatUser getOrCreate(Integer refId, UserType type) {

		return chatUserRepo.findByRefIdAndType(refId, type).orElseGet(() -> {

			ChatUser u = new ChatUser();
			u.setRefId(refId);
			u.setType(type);

			return chatUserRepo.save(u);
		});
	}

	@GetMapping("/student-search-ajax")
	@ResponseBody
	public List<Map<String, Object>> searchAjax(@RequestParam String keyword, HttpSession session) {
		Integer studentId = (Integer) session.getAttribute("studentId");
		if (studentId == null)
			return new ArrayList<>();

		ChatUser me = getOrCreate(studentId, UserType.STUDENT);
		List<Map<String, Object>> results = new ArrayList<>();

		List<Student> students = studentRepo.findByFullnameContainingIgnoreCase(keyword);
		for (Student s : students) {
			if (s.getStudid().equals(studentId))
				continue;

			ChatUser u = getOrCreate(s.getStudid(), UserType.STUDENT);

			Map<String, Object> map = new HashMap<>();
			map.put("id", u.getId());
			map.put("name", s.getFullname());
			map.put("type", "STUDENT");
			map.put("image", s.getProfileImage());
			map.put("status", getConnectionStatus(me, u));

			results.add(map);
		}

		List<Teacher> teachers = teacherRepo.findByFirstnameContainingIgnoreCaseOrLastnameContainingIgnoreCase(keyword,
				keyword);

		for (Teacher t : teachers) {
			ChatUser u = getOrCreate(t.getTeacherId(), UserType.TEACHER);
			TeacherProfile p = teacherProfileRepo.findByTeacherTeacherId(t.getTeacherId());

			Map<String, Object> map = new HashMap<>();
			map.put("id", u.getId());
			map.put("name", t.getFirstname() + " " + t.getLastname());
			map.put("type", "TEACHER");
			map.put("image", p != null ? p.getProfileImage() : null);
			map.put("status", getConnectionStatus(me, u));

			results.add(map);
		}

		return results;
	}

	@GetMapping("/student-profile-view")
	public String viewStudentProfile(@RequestParam Integer id, HttpSession session, Model model) {

		Integer myId = (Integer) session.getAttribute("studentId");
		if (myId == null)
			return "redirect:/student-login";

		Student student = studentRepo.findById(id).orElse(null);
		if (student == null)
			return "redirect:/student-search";

		ChatUser me = chatUserRepo.findByRefIdAndType(myId, UserType.STUDENT).orElse(null);
		ChatUser other = chatUserRepo.findByRefIdAndType(id, UserType.STUDENT).orElse(null);

		String status = "FOLLOW";

		if (me != null && other != null) {
			status = getConnectionStatus(me, other);
		}

		long connectionCount = connectionRepo.countBySenderRefIdOrReceiverRefId(id, id);

		model.addAttribute("student", student);
		model.addAttribute("status", status);
		model.addAttribute("connectionCount", connectionCount);
		model.addAttribute("chatUser", other);

		return "student-profile-view";
	}

}