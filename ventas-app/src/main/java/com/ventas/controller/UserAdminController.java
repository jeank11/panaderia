package com.ventas.controller;

import com.ventas.model.Usuario;
import com.ventas.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/usuarios")
public class UserAdminController {

    @Autowired
    private UsuarioRepository usuarioRepo;

    // ================================
    // LISTAR USUARIOS
    // ================================
    @GetMapping("/listar")
    public String listarUsuarios(Model model, HttpSession session) {

        Object roleObj = session.getAttribute("userRole");
        if (roleObj == null || !"ADMIN".equals(roleObj.toString())) {
            return "redirect:/productos?error=NoAutorizado";
        }

        List<Usuario> usuarios = usuarioRepo.findAll();

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", roleObj.toString());

        return "admin/usuarios/listar_usuarios";
    }

    // ================================
    // FORMULARIO NUEVO USUARIO
    // ================================
    @GetMapping("/nuevo")
    public String nuevoUsuarioForm(Model model, HttpSession session) {

        Object roleObj = session.getAttribute("userRole");
        if (roleObj == null || !"ADMIN".equals(roleObj.toString())) {
            return "redirect:/productos?error=NoAutorizado";
        }

        model.addAttribute("usuario", new Usuario());
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", roleObj.toString());

        return "admin/usuarios/nuevo_usuario";
    }

    // ================================
    // GUARDAR NUEVO USUARIO
    // ================================
    @PostMapping("/guardar")
    public String guardarNuevo(@ModelAttribute Usuario usuario, HttpSession session) {

        Object roleObj = session.getAttribute("userRole");
        if (roleObj == null || !"ADMIN".equals(roleObj.toString())) {
            return "redirect:/productos?error=NoAutorizado";
        }

        usuarioRepo.save(usuario);
        return "redirect:/admin/usuarios/listar";
    }

    // ================================
    // FORMULARIO EDITAR USUARIO
    // ================================
    @GetMapping("/editar/{id}")
    public String editarUsuarioForm(@PathVariable Long id, Model model, HttpSession session) {

        Object roleObj = session.getAttribute("userRole");
        if (roleObj == null || !"ADMIN".equals(roleObj.toString())) {
            return "redirect:/productos";
        }

        Usuario usuario = usuarioRepo.findById(id).orElse(null);
        if (usuario == null) return "redirect:/admin/usuarios/listar";

        model.addAttribute("usuario", usuario);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", roleObj.toString());

        return "admin/usuarios/editar_usuario";
    }

    // ================================
    // ACTUALIZAR USUARIO
    // ================================
    @PostMapping("/actualizar")
    public String actualizarUsuario(@ModelAttribute Usuario usuario, HttpSession session) {

        Object roleObj = session.getAttribute("userRole");
        if (roleObj == null || !"ADMIN".equals(roleObj.toString())) {
            return "redirect:/productos";
        }

        usuarioRepo.save(usuario);
        return "redirect:/admin/usuarios/listar";
    }

    // ================================
    // ELIMINAR USUARIO
    // ================================
    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, HttpSession session) {

        Object roleObj = session.getAttribute("userRole");
        if (roleObj == null || !"ADMIN".equals(roleObj.toString())) {
            return "redirect:/productos";
        }

        usuarioRepo.deleteById(id);
        return "redirect:/admin/usuarios/listar";
    }
}




