package com.example.demo.Controllers;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.*;
import com.example.demo.enums.ConnectionStatus;
import com.example.demo.enums.UserType;
import com.example.demo.repository.*;

import jakarta.servlet.http.HttpSession;

@Controller
public class StudentConnectionController {

    @Autowired private ChatUserRepository chatUserRepo;
    @Autowired private ConnectionRepository connectionRepo;
    @Autowired private StudentRepository studentRepo;
    @Autowired private TeacherRepository teacherRepo;
    @Autowired private TeacherProfileRepo teacherProfileRepo;

    // ================= 🔍 SEARCH =================
    @GetMapping("/student-search")
    public String searchUsers(@RequestParam(required = false) String keyword,
                              HttpSession session,
                              Model model) {

        Integer studentId = (Integer) session.getAttribute("studentId");
        if (studentId == null) return "redirect:/student-login";

        ChatUser me = getOrCreate(studentId, UserType.STUDENT);

        List<Map<String, Object>> results = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {

            // ===== STUDENTS =====
            List<Student> students = studentRepo.findByFullnameContainingIgnoreCase(keyword);

            for (Student s : students) {

                if (s.getStudid().equals(studentId)) continue;

                ChatUser u = getOrCreate(s.getStudid(), UserType.STUDENT);

                Map<String, Object> map = new HashMap<>();
                map.put("id", u.getId());
                map.put("name", u.getName());
                map.put("type", "STUDENT");
                map.put("image", u.getProfileImage());
                map.put("status", getConnectionStatus(me, u));

                results.add(map);
            }

            // ===== TEACHERS =====
            List<Teacher> teachers =
                    teacherRepo.findByFirstnameContainingIgnoreCaseOrLastnameContainingIgnoreCase(keyword, keyword);

            for (Teacher t : teachers) {

                ChatUser u = getOrCreate(t.getTeacherId(), UserType.TEACHER);

                Map<String, Object> map = new HashMap<>();
                map.put("id", u.getId());
                map.put("name", u.getName());
                map.put("type", "TEACHER");
                map.put("image", u.getProfileImage());
                map.put("status", getConnectionStatus(me, u));

                results.add(map);
            }
        }

        model.addAttribute("results", results);
        return "student-search";
    }

    // ================= ➕ SEND REQUEST =================
    @PostMapping("/connection/send")
    public String sendRequest(@RequestParam Integer receiverId,
                              HttpSession session) {

        Integer studentId = (Integer) session.getAttribute("studentId");
        if (studentId == null) return "redirect:/student-login";

        ChatUser sender = getOrCreate(studentId, UserType.STUDENT);
        ChatUser receiver = chatUserRepo.findById(receiverId).orElseThrow();

        // prevent duplicate
        Optional<Connection> existing =
                connectionRepo.findBySenderIdAndReceiverId(sender.getId(), receiver.getId());

        if (existing.isPresent()) return "redirect:/student-search";

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
        if (studentId == null) return "redirect:/student-login";

        ChatUser me = getOrCreate(studentId, UserType.STUDENT);

        // 🔹 Incoming requests
        List<Connection> received =
                connectionRepo.findByReceiverIdAndStatus(me.getId(), ConnectionStatus.PENDING);

        // 🔹 Sent requests
        List<Connection> sent =
                connectionRepo.findBySenderIdAndStatus(me.getId(), ConnectionStatus.PENDING);

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

        Optional<Connection> sent =
                connectionRepo.findBySenderIdAndReceiverId(me.getId(), other.getId());

        Optional<Connection> received =
                connectionRepo.findByReceiverIdAndSenderId(me.getId(), other.getId());

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

    // ================= 🧩 GET OR CREATE =================
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
                    if (p != null) {
                        u.setProfileImage(p.getProfileImage());
                    }
                }
            }

            return chatUserRepo.save(u);
        });
    }
}