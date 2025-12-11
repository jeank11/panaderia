package com.ventas.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/admin")
    public String adminPanel(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            return "redirect:/?error=NoAutorizado"; // muestra error si no es admin
        }

        model.addAttribute("userName", session.getAttribute("userName"));
        return "productos/lista";
    }
}


