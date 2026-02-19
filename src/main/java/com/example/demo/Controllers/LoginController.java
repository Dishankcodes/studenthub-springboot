package com.example.demo.Controllers;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.entity.Admin;
import com.example.demo.repository.AdminRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

	private AdminRepository adminrepo;

	public LoginController(AdminRepository adminrepo) {
		this.adminrepo = adminrepo;
	}

	@GetMapping("/admin-login")
	public String admin_login(Model model) {
		model.addAttribute("admin", new Admin());
		return "admin-login";
	}

	@PostMapping("/admin-dashboard")
	public String loginAdmin(
			@ModelAttribute("admin") Admin admin,
			HttpSession session, 
			Model model
			) {
		
		Optional<Admin> opt = adminrepo.findByEmail(admin.getEmail());

		if (opt.isEmpty() 
				|| !admin.getUsername().equals(opt.get().getUsername()) 
				|| !admin.getPassword().equals(opt.get().getPassword())) {
			
			model.addAttribute("error", "Admin not found or Invalid Password ");
			return "admin-login";
		}
			
		session.setAttribute("adminEmail", opt.get().getEmail());
		session.setAttribute("adminUsername", opt.get().getUsername());

		return "redirect:/admin-dashboard";
	}
	
	

}
