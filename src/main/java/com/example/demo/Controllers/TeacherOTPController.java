package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Service.EmailService;
import com.example.demo.repository.TeacherRepo;

import jakarta.servlet.http.HttpSession;

@Controller
public class TeacherOTPController {

	@Autowired
	private EmailService mailService;

	@Autowired
	private TeacherRepo teacherRepo;

	private String generateOtp()
	{
		return String.valueOf((int)(Math.random()*900000)+100000);
	}

	
	@GetMapping("/teacher-otp")
	public String otpTeacher()
	{
		return "teacher-otp";
	}
	
	@PostMapping("/teacher-send-otp")
	public String sendOtp(@RequestParam String email,
			HttpSession session,
			Model model)
	{
		if(!teacherRepo.existsByEmail(email))
		{
			model.addAttribute("error", "Email Not Registered");
			return "teacher-auth";
		}
		
		
		String otp = generateOtp();
		
		session.setAttribute("OTP", otp);
		session.setAttribute("OTP_EMAIL", email);
		session.setAttribute("OTP_TIME", System.currentTimeMillis());
		
		
		mailService.sendOtp(email,otp);
		
        model.addAttribute("email", email);
        
        return "redirect:/teacher-otp";

	}

	@PostMapping("/teacher-verify-otp")
	public String verifyOtp(@RequestParam String otp,
			HttpSession session,
			Model model
			)
	{
	
		String sessionOtp = (String) session.getAttribute("OTP");
        Long time = (Long) session.getAttribute("OTP_TIME");

        if (sessionOtp == null || time == null) {
            model.addAttribute("error", "OTP expired");
            return "teacher-otp";
        }

        if (System.currentTimeMillis() - time > 5 * 60 * 1000) {
            model.addAttribute("error", "OTP expired");
            return "teacher-otp";
        }

        if (!otp.equals(sessionOtp)) {
        	
        	model.addAttribute("otpSent", true);
            model.addAttribute("error", "Invalid OTP");
            return "teacher-otp";
        }

        // success
        session.removeAttribute("OTP");
        session.removeAttribute("OTP_TIME");
        session.removeAttribute("OTP_EMAIL");

        
     // mark logged in (important)
        session.setAttribute("TEACHER_LOGGED_IN", true);

        return "redirect:/teacher-dashboard";
    }
	}

