package com.example.demo.Controllers;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Admin;
import com.example.demo.repository.AdminRepository;


@Controller
public class LoginController {

	private AdminRepository adminrepo;
	
	public LoginController(AdminRepository adminrepo) {
        this.adminrepo = adminrepo;
    }
	
	
	@GetMapping("/teacher-auth") 
	public String teacher_login(){
		return "teacher-auth";
	}
	
	@GetMapping("/teacher-register")
	public String teacher_register() {
		return "teacher-register";
	}
	
	@GetMapping("/admin-login") 
	public String admin_login(Model model){
		model.addAttribute("admin", new Admin());
		return "admin-login";
	}
	
	@PostMapping("/admin-dashboard")
	public String loginAdmin(@RequestParam String email,
			@RequestParam String username,
			@RequestParam String password,
			Model model)
	{
		Optional<Admin> opt = adminrepo.findByEmail(email);

	    if (opt.isEmpty()) {
	        model.addAttribute("error", "Admin not found");
	        model.addAttribute("admin", new Admin());
	        return "admin-login";
	    }
	    
	    Admin dbAdmin = opt.get();

	    if (!password.equals(dbAdmin.getPassword())) {
	        model.addAttribute("error", "Invalid password");
	        model.addAttribute("admin", new Admin());
	        return "admin-login";
	    }
	    
		    model.addAttribute("username", dbAdmin.getUsername());
		    return "redirect:/admin-dashboard";
	}

}
