package com.example.demo.Controllers;

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
public class StudentProfileConnections {
	
	@Autowired
	private ConnectionRepository connectionRepo;
	
	@Autowired
	private ChatUserRepository chatUserRepo;
	
	@Autowired
	private StudentRepository studentRepo;

	@Autowired
	private TeacherRepository teacherRepo;

	@Autowired
	private TeacherProfileRepo teacherProfileRepo;

	@GetMapping("/student-connections")
	public String viewConnections(HttpSession session, Model model) {

	    Integer studentId = (Integer) session.getAttribute("studentId");
	    if (studentId == null) return "redirect:/student-login";

	    ChatUser me = chatUserRepo
	            .findByRefIdAndType(studentId, UserType.STUDENT)
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

	    Map<Integer, String> nameMap = new HashMap<>();
	    Map<Integer, String> imageMap = new HashMap<>();

	    for (ChatUser u : list) {

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

	    model.addAttribute("connections", list);
	    model.addAttribute("nameMap", nameMap);
	    model.addAttribute("imageMap", imageMap);

	    return "student-connections";
	}
	
	@PostMapping("/connection/remove")
	public String removeConnection(@RequestParam Integer userId, HttpSession session) {

	    Integer studentId = (Integer) session.getAttribute("studentId");
	    if (studentId == null) return "redirect:/student-login";

	    ChatUser me = chatUserRepo.findByRefIdAndType(studentId, UserType.STUDENT).orElse(null);
	    ChatUser other = chatUserRepo.findById(userId).orElse(null);

	    if (me == null || other == null) return "redirect:/student-connections";

	    connectionRepo.findBySenderIdAndReceiverIdOrSenderIdAndReceiverId(
	            me.getId(), other.getId(),
	            other.getId(), me.getId()
	    ).ifPresent(connectionRepo::delete);

	    return "redirect:/student-connections";
	}
}
