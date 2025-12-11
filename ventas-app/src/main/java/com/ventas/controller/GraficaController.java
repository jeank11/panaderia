package com.ventas.controller;

import com.ventas.model.Usuario;
import com.ventas.repository.PedidoRepository;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GraficaController {

    @Autowired
    private PedidoRepository pedidoRepo;

@GetMapping("/admin/pedidos_producto")
public String pedidosPorProducto(Model model, HttpSession session) {

    // Solo admin
     Usuario u = (Usuario) session.getAttribute("usuario"); // <-- CORREGIDO
    if (u == null) return "redirect:/login";

    model.addAttribute("userName", session.getAttribute("userName"));
    model.addAttribute("userRole", session.getAttribute("userRole"));

    List<Object[]> datos = pedidoRepo.cantidadPedidosPorProducto();
    model.addAttribute("datos", datos);

    return "admin/pedidos_producto"; // nombre del archivo HTML
}

@GetMapping("/admin/pedidos_por_producto_fecha")
public String pedidosPorProductoFecha(
        @RequestParam(required = false) String fechaInicio,
        @RequestParam(required = false) String fechaFin,
        HttpSession session, 
        Model model) {
    
    // Solo admin
     Usuario u = (Usuario) session.getAttribute("usuario"); // <-- CORREGIDO
    if (u == null) return "redirect:/login";

    model.addAttribute("userName", session.getAttribute("userName"));
    model.addAttribute("userRole", session.getAttribute("userRole"));

    LocalDate inicio = (fechaInicio != null && !fechaInicio.isEmpty()) ? LocalDate.parse(fechaInicio) : null;
    LocalDate fin = (fechaFin != null && !fechaFin.isEmpty()) ? LocalDate.parse(fechaFin) : null;

    List<Object[]> resultados = pedidoRepo.cantidadPedidosPorProductoYFechaEntrega(inicio, fin);

    model.addAttribute("resultados", resultados);
    model.addAttribute("fechaInicio", fechaInicio);
    model.addAttribute("fechaFin", fechaFin);

    return "admin/pedidos_por_producto_fecha"; // HTML que vamos a crear
}


}

