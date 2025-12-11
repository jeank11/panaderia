package com.ventas.controller;

import com.ventas.model.Producto;
import com.ventas.model.Usuario;
import com.ventas.model.Venta;
import com.ventas.repository.ProductoRepository;
import com.ventas.repository.UsuarioRepository;
import com.ventas.repository.VentaRepository;
import com.ventas.security.AdminOnly;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/admin/ventas")
public class AdminVentaController {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private VentaRepository ventaRepo;

    @GetMapping("/crear")
    public String crearForm(Model model, HttpSession session) {
        if (!AdminOnly.isAdmin(session)) return "redirect:/?error=NoAutorizado";

        model.addAttribute("usuarios", usuarioRepo.findAll());
        model.addAttribute("productos", productoRepo.findAll());

        return "admin/registrar_venta";
    }

    @PostMapping("/crear")
public String crearVenta(@RequestParam("usuarioId") Long usuarioId,
                         @RequestParam("productoId") Long productoId,
                         @RequestParam("cantidad") Integer cantidad,
                         HttpSession session) {

        if (!AdminOnly.isAdmin(session)) return "redirect:/?error=NoAutorizado";

        Usuario u = usuarioRepo.findById(usuarioId).orElse(null);
        Producto p = productoRepo.findById(productoId).orElse(null);

        if (u == null || p == null) return "redirect:/admin/ventas/crear?error=DatosInvalidos";

        Venta v = new Venta();
        v.setUsuario(u);
        v.setProducto(p);
        v.setCantidad(cantidad);

        ventaRepo.save(v);

        return "redirect:/admin/ventas/crear?success=ok";
    }

@GetMapping("/listar")
public String listarVentas(
        @RequestParam(required = false) Long usuarioId,
        Model model,
        HttpSession session) {

    if (!AdminOnly.isAdmin(session)) return "redirect:/?error=NoAutorizado";

    model.addAttribute("usuarios", usuarioRepo.findAll());
    model.addAttribute("usuarioId", usuarioId);

    List<Venta> ventas;

    Usuario usuarioSeleccionado = null;

    if (usuarioId != null) {
        usuarioSeleccionado = usuarioRepo.findById(usuarioId).orElse(null);

        if (usuarioSeleccionado != null) {
            ventas = ventaRepo.findByUsuario(usuarioSeleccionado);
        } else {
            ventas = ventaRepo.findAll();
        }
    } else {
        ventas = ventaRepo.findAll();
    }

    model.addAttribute("ventas", ventas);
    model.addAttribute("usuarioSeleccionado", usuarioSeleccionado);

    return "admin/listar_ventas";
}




@GetMapping("/lista/usuario/{id}")
public String listarVentasPorUsuario(@PathVariable("id") Long usuarioId,
                                     Model model,
                                     HttpSession session) {

    if (!AdminOnly.isAdmin(session)) return "redirect:/?error=NoAutorizado";

    Usuario usuario = usuarioRepo.findById(usuarioId).orElse(null);

    if (usuario == null) return "redirect:/admin/ventas/lista?error=UsuarioNoEncontrado";

    model.addAttribute("ventas", ventaRepo.findByUsuario(usuario));
    model.addAttribute("usuarios", usuarioRepo.findAll());
    model.addAttribute("usuarioSeleccionado", usuario);

    return "admin/listar_ventas";
}

}

