package com.example.demo.Controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
import com.example.demo.enums.ApplicationStatus;
import com.example.demo.enums.ConnectionStatus;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.ChatUserRepository;
import com.example.demo.repository.ConnectionRepository;
import com.example.demo.repository.StudentRepository;

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

	@GetMapping("/student-profile")
	public String studentProfile(HttpSession session, Model model, @RequestParam(required = false) Boolean edit) {

		Integer studentId = (Integer) session.getAttribute("studentId");
		if (studentId == null)
			return "redirect:/student-login";

		Student student = studentRepo.findById(studentId).orElseThrow();

		List<InternshipApplication> completedInternships = applicationRepo.findByStudent_Studid(studentId).stream()
				.filter(app -> app.getStatus() == ApplicationStatus.COMPLETED).toList();

		ChatUser me = chatUserRepo
		        .findByRefIdAndType(studentId, com.example.demo.enums.UserType.STUDENT)
		        .orElse(null);

		List<ChatUser> list = new ArrayList<>();

		if (me != null) {

		    List<Connection> connections =
		            connectionRepo.findBySenderIdAndStatusOrReceiverIdAndStatus(
		                    me.getId(), ConnectionStatus.ACCEPTED,
		                    me.getId(), ConnectionStatus.ACCEPTED
		            );

		    for (Connection c : connections) {

		        ChatUser other = c.getSender().getId().equals(me.getId())
		                ? c.getReceiver()
		                : c.getSender();

		        list.add(other);
		    }
		}

		model.addAttribute("connections", list);
		model.addAttribute("completedInternships", completedInternships);
		model.addAttribute("student", student);
		model.addAttribute("editMode", edit != null && edit);

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

		if (!student.getEmail().equals(oldEmail) || !student.getPassword().equals(password)) {

			model.addAttribute("emailError", "Invalid credentials");
			model.addAttribute("student", student);
			return "student-profile-edit";
		}

		student.setEmail(newEmail);
		studentRepo.save(student);

		return "redirect:/student-profile";
	}

	@PostMapping("/student-profile/change-password")
	public String changePassword(String oldPassword, String newPassword, String confirmPassword, HttpSession session,
			Model model) {

		Integer studentId = (Integer) session.getAttribute("studentId");
		if (studentId == null)
			return "redirect:/student-login";

		Student student = studentRepo.findById(studentId).orElseThrow();

		if (!student.getPassword().equals(oldPassword)) {
			model.addAttribute("passwordError", "Old password incorrect");
			model.addAttribute("student", student);
			return "student-profile-edit";
		}

		if (!newPassword.equals(confirmPassword)) {
			model.addAttribute("passwordError", "Passwords do not match");
			model.addAttribute("student", student);
			return "student-profile-edit";
		}

		student.setPassword(newPassword);
		studentRepo.save(student);

		return "redirect:/student-profile";
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
