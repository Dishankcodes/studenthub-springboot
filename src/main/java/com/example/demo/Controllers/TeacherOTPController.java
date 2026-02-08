package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.Service.EmailService;
import com.example.demo.repository.TeacherRepo;

import jakarta.servlet.http.HttpSession;

@Controller
public class TeacherOTPController {

	@Autowired
	private EmailService mailService;

	@Autowired
	private TeacherRepo teacherRepo;

	private String generateOtp() {
		return String.valueOf((int) (Math.random() * 900000) + 100000);
	}

	/* ================= OTP PAGE ================= */
	@GetMapping("/teacher-otp")
	public String otpTeacher(HttpSession session) {
		if (session.getAttribute("OTP_EMAIL") == null) {
			return "redirect:/teacher-auth";
		}
		return "teacher-otp";
	}

	/* ================= SEND OTP ================= */
	@PostMapping("/teacher-send-otp")
	public String sendOtp(@RequestParam String email, HttpSession session, RedirectAttributes redirectAttributes) {

		if (!teacherRepo.existsByEmail(email)) {
			redirectAttributes.addFlashAttribute("error", "Email not registered");
			return "redirect:/teacher-auth";
		}

		String otp = generateOtp();
		long now = System.currentTimeMillis();

		session.setAttribute("OTP", otp);
		session.setAttribute("OTP_EMAIL", email);
		session.setAttribute("OTP_TIME", now);
		session.setAttribute("OTP_LAST_SENT", now);

		mailService.sendOtp(email, otp);

		redirectAttributes.addFlashAttribute("otpSent", true);
		return "redirect:/teacher-otp";
	}

	/* ================= VERIFY OTP ================= */
	@PostMapping("/teacher-verify-otp")
	public String verifyOtp(@RequestParam String otp, HttpSession session, RedirectAttributes redirectAttributes) {

		String sessionOtp = (String) session.getAttribute("OTP");
		Long time = (Long) session.getAttribute("OTP_TIME");

		if (sessionOtp == null || time == null) {
			redirectAttributes.addFlashAttribute("error", "OTP expired");
			return "redirect:/teacher-otp";
		}

		if (System.currentTimeMillis() - time > 5 * 60 * 1000) {
			redirectAttributes.addFlashAttribute("error", "OTP expired");
			return "redirect:/teacher-otp";
		}

		if (!otp.equals(sessionOtp)) {
			redirectAttributes.addFlashAttribute("error", "Invalid OTP");
			return "redirect:/teacher-otp";
		}

		// SUCCESS
		// SUCCESS
		String email = (String) session.getAttribute("OTP_EMAIL");

		var teacher = teacherRepo.findByemail(email);
		if (teacher == null) {
			redirectAttributes.addFlashAttribute("error", "Teacher not found");
			return "redirect:/teacher-auth";
		}

		session.setAttribute("TEACHER_LOGGED_IN", true);
		session.setAttribute("teacherId", teacher.getTeacherId());

		// cleanup OTP data
		session.removeAttribute("OTP");
		session.removeAttribute("OTP_TIME");
		session.removeAttribute("OTP_LAST_SENT");
		session.removeAttribute("OTP_EMAIL");

		return "redirect:/teacher-dashboard";

	}

	/* ================= RESEND OTP ================= */
	@PostMapping("/teacher-resend-otp")
	public String resendOtp(HttpSession session, RedirectAttributes redirectAttributes) {

		String email = (String) session.getAttribute("OTP_EMAIL");
		Long lastSent = (Long) session.getAttribute("OTP_LAST_SENT");

		if (email == null) {
			redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
			return "redirect:/teacher-auth";
		}

		long now = System.currentTimeMillis();

		if (lastSent != null && (now - lastSent) < 30_000) {
			redirectAttributes.addFlashAttribute("error", "Please wait before requesting a new OTP.");
			return "redirect:/teacher-otp";
		}

		String newOtp = generateOtp();

		session.setAttribute("OTP", newOtp);
		session.setAttribute("OTP_TIME", now);
		session.setAttribute("OTP_LAST_SENT", now);

		mailService.sendOtp(email, newOtp);

		redirectAttributes.addFlashAttribute("otpResent", true);
		return "redirect:/teacher-otp";
	}
}
