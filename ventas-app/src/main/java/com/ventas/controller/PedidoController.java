package com.ventas.controller;

import com.ventas.model.Pedido;
import com.ventas.model.Producto;
import com.ventas.model.Usuario;
import com.ventas.repository.PedidoRepository;
import com.ventas.repository.ProductoRepository;


import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoRepository pedidoRepo;



    @Autowired
    private ProductoRepository productoRepo;

    // FORMULARIO PARA CREAR PEDIDO
   @GetMapping("/nuevo")
    public String nuevoPedido(Model model, HttpSession session) {

    model.addAttribute("userName", session.getAttribute("userName"));
    model.addAttribute("userRole", session.getAttribute("userRole"));

    model.addAttribute("pedido", new Pedido());
    model.addAttribute("productos", productoRepo.findAll());
    return "pedidos/nuevo_pedido";
}


    // GUARDAR PEDIDO
  // GUARDAR PEDIDO
@PostMapping("/guardar")
public String guardarPedido(@RequestParam Long productoId,
                            @RequestParam int cantidad, 
                            @RequestParam String direccion,
                            @RequestParam String fechaEntrega,
                            HttpSession session) {

    Usuario u = (Usuario) session.getAttribute("usuario");  // <-- CORREGIDO

    Producto p = productoRepo.findById(productoId).orElse(null);

    Pedido pedido = new Pedido();
    pedido.setUsuario(u);
    pedido.setProducto(p);
    pedido.setCantidad(cantidad);
    pedido.setDireccion(direccion);
    pedido.setFechaPedido(LocalDate.now());
    pedido.setFechaEntrega(LocalDate.parse(fechaEntrega));
    pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);

    pedidoRepo.save(pedido);

    return "redirect:/pedidos/mis_pedidos?ok";
}


// LISTA DE PEDIDOS DEL USUARIO
@GetMapping("/mis_pedidos")
public String misPedidos(Model model, HttpSession session) {

    Usuario u = (Usuario) session.getAttribute("usuario"); // <-- CORREGIDO
    if (u == null) return "redirect:/login";

    model.addAttribute("userName", session.getAttribute("userName"));
    model.addAttribute("userRole", session.getAttribute("userRole"));

    List<Pedido> pedidos = pedidoRepo.findByUsuario(u);
    model.addAttribute("pedidos", pedidos);

    return "pedidos/listar_pedidos";
}




    // LISTA DE TODOS LOS PEDIDOS (solo admin)
   @GetMapping("/listar")
    public String listarPedidos(Model model, HttpSession session) {

    model.addAttribute("userName", session.getAttribute("userName"));
    model.addAttribute("userRole", session.getAttribute("userRole"));

    model.addAttribute("pedidos", pedidoRepo.findAll());
    return "pedidos/listar_pedidos";
}
// FORMULARIO EDITAR
@GetMapping("/editar/{id}")
public String editarPedido(@PathVariable Long id, Model model, HttpSession session) {

    Pedido pedido = pedidoRepo.findById(id).orElse(null);
    if (pedido == null) return "redirect:/pedidos/listar?error=NoEncontrado";

    model.addAttribute("userName", session.getAttribute("userName"));
    model.addAttribute("userRole", session.getAttribute("userRole"));

    model.addAttribute("pedido", pedido);
    model.addAttribute("productos", productoRepo.findAll());

    return "pedidos/editar_pedido";
}
@PostMapping("/actualizar")
public String actualizarPedido(@RequestParam Long id,
                               @RequestParam Long productoId,
                               @RequestParam int cantidad,
                               @RequestParam String direccion,
                               @RequestParam String fechaEntrega,
                               @RequestParam(required = false) String estado,
                               HttpSession session) {

    Pedido pedido = pedidoRepo.findById(id).orElse(null);
    if (pedido == null) return "redirect:/pedidos/listar?error=NoEncontrado";

    pedido.setProducto(productoRepo.findById(productoId).orElse(null));
    pedido.setCantidad(cantidad);
    pedido.setDireccion(direccion);
    pedido.setFechaEntrega(LocalDate.parse(fechaEntrega));

        if (estado != null) {
        try {
            pedido.setEstado(Pedido.EstadoPedido.valueOf(estado));
        } catch (IllegalArgumentException e) {
            // ignorar o loguear, estado inválido
        }
    }

    pedidoRepo.save(pedido);

    return "redirect:/pedidos/listar?editOk";
}
@GetMapping("/eliminar/{id}")
public String eliminarPedido(@PathVariable Long id) {

    if (pedidoRepo.existsById(id)) {
        pedidoRepo.deleteById(id);
    }

    return "redirect:/pedidos/listar?deleteOk";
}

@GetMapping("/filtrar")
public String filtrarPedidos(@RequestParam("q") String q,
                             Model model,
                             HttpSession session) {

    // Validación ADMIN
    String role = (String) session.getAttribute("userRole");
    if (role == null || !role.equals("ADMIN")) {
        return "redirect:/?error=NoAutorizado";
    }

    List<Pedido> resultados;

    // Si q es número → buscar por ID
    if (q.matches("\\d+")) {
        Long id = Long.parseLong(q);
        Pedido pedido = pedidoRepo.findById(id).orElse(null);

        if (pedido != null) {
            resultados = List.of(pedido);
        } else {
            resultados = List.of();
        }
    } else {
        // BÚSQUEDA POR DIRECCIÓN, PRODUCTO Y USUARIO
        List<Pedido> porDireccion = pedidoRepo.findByDireccionContainingIgnoreCase(q);
        List<Pedido> porProducto  = pedidoRepo.findByProductoNombreContainingIgnoreCase(q);
        List<Pedido> porUsuario   = pedidoRepo.findByUsuarioNombreContainingIgnoreCase(q);

        // Unir sin duplicar
        Set<Pedido> set = new HashSet<>();
        set.addAll(porDireccion);
        set.addAll(porProducto);
        set.addAll(porUsuario);

        resultados = new ArrayList<>(set);
    }

    model.addAttribute("pedidos", resultados);

    // navbar info
    model.addAttribute("userName", session.getAttribute("userName"));
    model.addAttribute("userRole", session.getAttribute("userRole"));

    return "pedidos/listar_pedidos";
}



}
