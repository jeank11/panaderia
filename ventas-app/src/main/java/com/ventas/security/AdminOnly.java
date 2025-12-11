package com.ventas.security;

import jakarta.servlet.http.HttpSession;

public class AdminOnly {

    public static boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return role != null && role.equals("ADMIN");
    }
}

