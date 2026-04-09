package com.example.demo.Controllers;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Admin;
import com.example.demo.enums.AdminRole;
import com.example.demo.repository.AdminRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminManagementController {

    private AdminRepository adminRepo;
    private PasswordEncoder passwordEncoder;

    public AdminManagementController(AdminRepository adminRepo, PasswordEncoder passwordEncoder) {
        this.adminRepo = adminRepo;
        this.passwordEncoder = passwordEncoder;
    }


    @GetMapping("/admin-settings")
    public String adminSettings(HttpSession session, Model model) {

        if (session.getAttribute("adminEmail") == null) {
            session.setAttribute("redirectAfterLogin", "/admin-settings");
            return "redirect:/admin-login";
        }

        AdminRole role = (AdminRole) session.getAttribute("adminRole");

        if (role != AdminRole.SUPER_ADMIN) {
            return "redirect:/access-denied";
        }

        model.addAttribute("admin", new Admin());
        model.addAttribute("admins", adminRepo.findAll());
        model.addAttribute("roles", AdminRole.values());

        return "admin-settings";
    }


    @PostMapping("/save-admin")
    public String saveAdmin(@ModelAttribute Admin admin,
                            @RequestParam("role") String role,
                            HttpSession session) {

        AdminRole currentRole = (AdminRole) session.getAttribute("adminRole");

        if (currentRole != AdminRole.SUPER_ADMIN) {
            return "redirect:/access-denied";
        }

        admin.setRole(AdminRole.valueOf(role));
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));

        adminRepo.save(admin);

        return "redirect:/admin-settings";
    }
}