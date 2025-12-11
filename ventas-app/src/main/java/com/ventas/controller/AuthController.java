package com.ventas.controller;

import com.ventas.model.Usuario;
import com.ventas.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepo;

   @GetMapping({"/", "/index"})
    public String index() {
    return "redirect:/login";
}


    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "register";
    }

  @PostMapping("/register")
public String register(Usuario usuario, Model model) {
    if (usuarioRepo.findByEmail(usuario.getEmail()) != null) {
        model.addAttribute("error", "El email ya está registrado");
        return "register";
    }
    // asegurar rol por defecto
    if (usuario.getRole() == null || usuario.getRole().isBlank()) {
        usuario.setRole("USER");
    }
    usuarioRepo.save(usuario);
    return "redirect:/login";
}


    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

@PostMapping("/login")
public String login(@RequestParam("email") String email,
                    @RequestParam("password") String password,
                    HttpSession session,
                    Model model) {
    Usuario u = usuarioRepo.findByEmail(email);
    if (u == null || !u.getPassword().equals(password)) {
        model.addAttribute("error", "Credenciales inválidas");
        return "login";
    }
    session.setAttribute("userId", u.getId());
    session.setAttribute("userName", u.getNombre());
    session.setAttribute("userRole", u.getRole());  // <- CORRECTO
    session.setAttribute("usuario", u);
    return "redirect:/productos";
}



    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
