package com.example.demo.Controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.ChatUser;
import com.example.demo.entity.Connection;
import com.example.demo.entity.InternshipApplication;
import com.example.demo.entity.Student;
import com.example.demo.entity.Teacher;
import com.example.demo.entity.TeacherProfile;
import com.example.demo.enums.ApplicationStatus;
import com.example.demo.enums.ConnectionStatus;
import com.example.demo.enums.UserType;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.ChatUserRepository;
import com.example.demo.repository.ConnectionRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TeacherProfileRepo;
import com.example.demo.repository.TeacherRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class StudentProfileController {

	@Autowired
	private StudentRepository studentRepo;

	private static final String UPLOAD_BASE = System.getProperty("user.dir") + File.separator + "uploads";

	@Autowired
	private ApplicationRepository applicationRepo;

	@Autowired
	private ConnectionRepository connectionRepo;

	@Autowired
	private ChatUserRepository chatUserRepo;

	@Autowired
	private TeacherRepository teacherRepo;

	@Autowired
	private TeacherProfileRepo teacherProfileRepo;

	@GetMapping("/student-profile")
	public String studentProfile(HttpSession session, Model model, @RequestParam(required = false) Boolean edit) {

		Integer studentId = (Integer) session.getAttribute("studentId");
		if (studentId == null)
			return "redirect:/student-login";

		Student student = studentRepo.findById(studentId).orElseThrow();

		boolean isEdit = (edit != null && edit);

		List<InternshipApplication> completedInternships = applicationRepo.findByStudent_Studid(studentId).stream()
				.filter(app -> app.getStatus() == ApplicationStatus.COMPLETED).toList();

		ChatUser me = chatUserRepo.findByRefIdAndType(studentId, com.example.demo.enums.UserType.STUDENT).orElse(null);

		List<ChatUser> list = new ArrayList<>();

		if (me != null) {

			List<Connection> connections = connectionRepo.findBySenderIdAndStatusOrReceiverIdAndStatus(me.getId(),
					ConnectionStatus.ACCEPTED, me.getId(), ConnectionStatus.ACCEPTED);

			for (Connection c : connections) {

				ChatUser other = c.getSender().getId().equals(me.getId()) ? c.getReceiver() : c.getSender();

				list.add(other);
			}
		}

		Map<Integer, String> nameMap = new HashMap<>();
		Map<Integer, String> imageMap = new HashMap<>();

		for (ChatUser u : list) {

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

		long connectionCount = list.size();
		model.addAttribute("connectionCount", connectionCount);

		model.addAttribute("nameMap", nameMap);
		model.addAttribute("imageMap", imageMap);
		model.addAttribute("connections", list);
		model.addAttribute("completedInternships", completedInternships);
		model.addAttribute("student", student);
		model.addAttribute("editMode", isEdit);

		return "student-profile";
	}

	@PostMapping("/student-profile/update")
	public String updateProfile(@RequestParam String fullname, @RequestParam String college,
			@RequestParam String degree, @RequestParam(required = false) List<String> interests, HttpSession session) {

		Integer studentId = (Integer) session.getAttribute("studentId");
		if (studentId == null)
			return "redirect:/student-login";

		Student student = studentRepo.findById(studentId).orElseThrow();

		student.setFullname(fullname);
		student.setCollege(college);
		student.setDegree(degree);

		if (interests != null) {
			student.setInterests(String.join(", ", interests));
		}

		studentRepo.save(student);
		return "redirect:/student-profile";
	}

	@PostMapping("/student-profile/change-email")
	public String changeEmail(String oldEmail, String password, String newEmail, HttpSession session, Model model) {

		Integer studentId = (Integer) session.getAttribute("studentId");
		if (studentId == null)
			return "redirect:/student-login";

		Student student = studentRepo.findById(studentId).orElseThrow();

		if (oldEmail == null || password == null || newEmail == null || oldEmail.trim().isEmpty()
				|| password.trim().isEmpty() || newEmail.trim().isEmpty()) {

			model.addAttribute("emailError", "All fields are required");
			return studentProfile(session, model, false); // 🔥 FIX
		}

		if (!student.getEmail().equals(oldEmail) || !student.getPassword().equals(password)) {

			model.addAttribute("emailError", "Old email or password is incorrect");
			return studentProfile(session, model, false);
		}

		if (student.getEmail().equals(newEmail)) {

			model.addAttribute("emailError", "New email must be different");
			return studentProfile(session, model, false);
		}

		student.setEmail(newEmail);
		studentRepo.save(student);

		model.addAttribute("emailSuccess", "Email updated successfully ✅");
		return studentProfile(session, model, false);
	}

	// ===================== CHANGE PASSWORD =====================
	@PostMapping("/student-profile/change-password")
	public String changePassword(String oldPassword, String newPassword, String confirmPassword, HttpSession session,
			Model model) {

		Integer studentId = (Integer) session.getAttribute("studentId");
		if (studentId == null)
			return "redirect:/student-login";

		Student student = studentRepo.findById(studentId).orElseThrow();

		if (oldPassword == null || newPassword == null || confirmPassword == null || oldPassword.isEmpty()
				|| newPassword.isEmpty() || confirmPassword.isEmpty()) {

			model.addAttribute("passwordError", "All fields are required");
			return studentProfile(session, model, false);
		}

		if (!student.getPassword().equals(oldPassword)) {
			model.addAttribute("passwordError", "Old password is incorrect");
			return studentProfile(session, model, false);
		}

		if (!newPassword.equals(confirmPassword)) {
			model.addAttribute("passwordError", "Passwords do not match");
			return studentProfile(session, model, false);
		}

		if (oldPassword.equals(newPassword)) {
			model.addAttribute("passwordError", "New password must be different");
			return studentProfile(session, model, false);
		}

		student.setPassword(newPassword);
		studentRepo.save(student);

		model.addAttribute("passwordSuccess", "Password changed successfully 🔒");
		return studentProfile(session, model, false);
	}

	@PostMapping("/student-profile/upload-image")
	public String uploadProfileImage(@RequestParam("image") MultipartFile file, HttpSession session)
			throws IOException {

		Integer studentId = (Integer) session.getAttribute("studentId");
		if (studentId == null)
			return "redirect:/student-login";

		Student student = studentRepo.findById(studentId).orElseThrow();

		String uploadDir = "uploads/profile/";
		Files.createDirectories(Paths.get(uploadDir));

		String fileName = "student_" + studentId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
		Path path = Paths.get(uploadDir + fileName);
		Files.write(path, file.getBytes());

		student.setProfileImage("/" + uploadDir + fileName);
		studentRepo.save(student);

		return "redirect:/student-profile";
	}
}
