package com.ventas.controller;

import com.ventas.model.Producto;
import com.ventas.repository.ProductoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepo;

    // LISTAR + BUSCAR + ORDEN ALFABÉTICO
    @GetMapping
    public String listarProductos(@RequestParam(value = "q", required = false) String q,
                                  Model model,
                                  HttpSession session) {

        // Navbar
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        List<Producto> productos;

        if (q != null && !q.isBlank()) {
            // BUSCAR + ORDENAR
            productos = productoRepo
                    .findByNombreContainingIgnoreCaseOrderByNombreAsc(q);
            model.addAttribute("q", q);
        } else {
            // LISTAR TODO ORDENADO
            productos = productoRepo.findAllByOrderByNombreAsc();
        }

        model.addAttribute("productos", productos);
        return "productos/lista";
    }


    // Formulario para crear producto (solo ADMIN)
    @GetMapping("/nuevo")
    public String nuevoProducto(HttpSession session, Model model) {
        String role = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(role)) {
            return "redirect:/productos?error=NoAutorizado";
        }

        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        model.addAttribute("producto", new Producto());
        return "productos/crear";
    }

    // Guardar producto (solo ADMIN)
    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto, HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(role)) {
            return "redirect:/productos?error=NoAutorizado";
        }

        productoRepo.save(producto);
        return "redirect:/productos";
    }


    @GetMapping("/editar/{id}")
    public String editarProductoForm(@PathVariable("id") Long id, Model model, HttpSession session) {
        Object roleObj = session.getAttribute("userRole");
        if (roleObj == null || !"ADMIN".equals(roleObj.toString())) {
            return "redirect:/productos";
        }

        Producto p = productoRepo.findById(id).orElse(null);
        if (p == null) return "redirect:/productos";

        model.addAttribute("producto", p);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", roleObj.toString());
        return "productos/editar_producto";
    }


    @PostMapping("/editar/{id}")
    public String editarProducto(@PathVariable("id") Long id,
                                 @RequestParam("nombre") String nombre,
                                 @RequestParam("precio") Double precio,
                                 HttpSession session) {

        Object roleObj = session.getAttribute("userRole");
        if (roleObj == null || !"ADMIN".equals(roleObj.toString())) {
            return "redirect:/productos";
        }

        Producto p = productoRepo.findById(id).orElse(null);
        if (p == null) return "redirect:/productos";

        p.setNombre(nombre);
        p.setPrecio(precio);
        productoRepo.save(p);

        return "redirect:/productos";
    }


    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable("id") Long id, HttpSession session) {
        Object roleObj = session.getAttribute("userRole");
        if (roleObj == null || !"ADMIN".equals(roleObj.toString())) {
            return "redirect:/productos";
        }

        productoRepo.deleteById(id);
        return "redirect:/productos";
    }
}





