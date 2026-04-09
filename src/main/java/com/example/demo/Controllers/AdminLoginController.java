package com.example.demo.Controllers;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.entity.Admin;
import com.example.demo.repository.AdminRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminLoginController {

	private AdminRepository adminrepo;

	private BCryptPasswordEncoder passwordEncoder;

	public AdminLoginController(AdminRepository adminrepo, BCryptPasswordEncoder passwordEncoder) {
		this.adminrepo = adminrepo;
		this.passwordEncoder = 	passwordEncoder;
	}

	@GetMapping("/admin-login")
	public String admin_login(Model model) {
		model.addAttribute("admin", new Admin());
		return "admin-login";
	}

	@PostMapping("/admin-dashboard")
	public String loginAdmin(@ModelAttribute("admin") Admin admin, HttpSession session, Model model) {

		Optional<Admin> opt = adminrepo.findByEmail(admin.getEmail());

		if (opt.isEmpty() 
				|| !admin.getUsername().equals(opt.get().getUsername())
				|| !passwordEncoder.matches(admin.getPassword(),opt.get().getPassword())) {

			model.addAttribute("error", "Admin not found or Invalid Password ");
			return "admin-login";
		}

		session.setAttribute("adminEmail", opt.get().getEmail());
		session.setAttribute("adminUsername", opt.get().getUsername());

		return "redirect:/admin-dashboard";
	}

}
