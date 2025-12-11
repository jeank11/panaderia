package com.ventas.controller;

import com.ventas.model.Producto;
import com.ventas.model.Usuario;
import com.ventas.model.Venta;
import com.ventas.repository.ProductoRepository;
import com.ventas.repository.UsuarioRepository;
import com.ventas.repository.VentaRepository;

import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ventas")
public class VentaController {

    @Autowired
    private VentaRepository ventaRepo;

    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    // ------------------------------------------
    //         LISTAR VENTAS (PANEL ADMIN)
    // ------------------------------------------
    @GetMapping("/listar")
    public String listarVentas(
            @RequestParam(name = "usuarioId", required = false) Long usuarioId,
            Model model,
            HttpSession session) {

        Object roleObj = session.getAttribute("userRole");
        if (roleObj == null || !"ADMIN".equals(roleObj.toString())) {
            return "redirect:/?error=NoAutorizado";
        }

        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        model.addAttribute("usuarios", usuarioRepo.findAll());
        model.addAttribute("usuarioId", usuarioId);

        List<Venta> ventas;
        Usuario usuarioSeleccionado = null;

        if (usuarioId != null) {
            usuarioSeleccionado = usuarioRepo.findById(usuarioId).orElse(null);
            ventas = ventaRepo.findByUsuario(usuarioSeleccionado);
        } else {
            ventas = ventaRepo.findAll();
        }

        model.addAttribute("ventas", ventas);
        model.addAttribute("usuarioSeleccionado", usuarioSeleccionado);

        double total = ventas.stream()
                .mapToDouble(v -> v.getCantidad() * v.getProducto().getPrecio())
                .sum();

        model.addAttribute("totalGeneral", total);

        return "admin/listar_ventas";
    }


    // ------------------------------------------
    //        MIS COMPRAS (USUARIO NORMAL)
    // ------------------------------------------
@GetMapping("/mis_compras")
public String misCompras(Model model, HttpSession session) {

    Long userId = (Long) session.getAttribute("userId");
    if (userId == null) return "redirect:/login";

    Usuario u = usuarioRepo.findById(userId).orElse(null);
    List<Venta> ventas = ventaRepo.findByUsuario(u);

    Map<Long, Double> subtotales = new HashMap<>();
    double totalPendiente = 0;

    // Filtrar solo compras NO pagadas
    List<Venta> pendientes = ventas.stream()
            .filter(v -> "PENDIENTE".equals(v.getEstado()) ||
                         "ESPERANDO_CONFIRMACION".equals(v.getEstado()))
            .toList();

    for (Venta v : pendientes) {
        double subtotal = v.getProducto().getPrecio() * v.getCantidad();
        subtotales.put(v.getId(), subtotal);
        totalPendiente += subtotal;
    }

    model.addAttribute("ventas", pendientes);
    model.addAttribute("subtotales", subtotales);
    model.addAttribute("totalGeneral", totalPendiente);

    model.addAttribute("userName", session.getAttribute("userName"));
    model.addAttribute("userRole", session.getAttribute("userRole"));

    return "ventas/mis_compras";
}


// ------------------------------------------
//        HISTORIAL DE COMPRAS (USUARIO)
// ------------------------------------------
@GetMapping("/historial")
public String historialCompras(Model model, HttpSession session) {

    Long userId = (Long) session.getAttribute("userId");
    if (userId == null) return "redirect:/login";

    Usuario u = usuarioRepo.findById(userId).orElse(null);
    List<Venta> ventas = ventaRepo.findByUsuario(u);

    Map<Long, Double> subtotales = new HashMap<>();

    for (Venta v : ventas) {
        double subtotal = v.getProducto().getPrecio() * v.getCantidad();
        subtotales.put(v.getId(), subtotal);
    }

    // Filtrar SOLO compras con estado PAGADO o ENTREGADO
    List<Venta> historial = ventas.stream()
            .filter(v -> "PAGADO".equals(v.getEstado()) ||
                         "ENTREGADO".equals(v.getEstado()))
            .toList();

    model.addAttribute("ventas", historial);
    model.addAttribute("subtotales", subtotales);

    model.addAttribute("userName", session.getAttribute("userName"));
    model.addAttribute("userRole", session.getAttribute("userRole"));

    return "ventas/historial";
}




    // ------------------------------------------
    //     ⭐ PASO 5 – PAGAR COMPRAS CORREGIDO ⭐
    // ------------------------------------------

    // 1️⃣ Selección de método de pago
    @PostMapping("/mis_compras/pagar")
    public String seleccionarMetodoPago(
            @RequestParam(value = "ids", required = false) List<Long> ids,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttrs) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        if (ids == null || ids.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "No seleccionaste ninguna compra.");
            return "redirect:/ventas/mis_compras";
        }

        model.addAttribute("ids", ids);
        return "ventas/metodo_pago";
    }

    // 2️⃣ Mostrar datos de transferencia
@PostMapping("/mis_compras/pagar/transferencia")
public String pagoPorTransferencia(
        @RequestParam("ids") List<Long> ids,
        Model model,
        HttpSession session) {

    Long userId = (Long) session.getAttribute("userId");
    if (userId == null) return "redirect:/login";

    List<Venta> ventas = ventaRepo.findAllById(ids);
    model.addAttribute("ventas", ventas);
    model.addAttribute("ids", ids);

    // 👉 CALCULAR TOTAL
    double total = ventas.stream()
            .mapToDouble(v -> v.getCantidad() * v.getProducto().getPrecio())
            .sum();

    // 👉 ENVIARLO A LA VISTA
    model.addAttribute("total", total);

    return "ventas/transferencia";
}


    // 3️⃣ Confirmación de transferencia por usuario
    @PostMapping("/mis_compras/confirmar-transferencia")
    public String confirmarTransferencia(
            @RequestParam("ids") List<Long> ids,
            HttpSession session,
            RedirectAttributes redirectAttrs) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        for (Long id : ids) {
            ventaRepo.findById(id).ifPresent(v -> {
                if (v.getUsuario().getId().equals(userId)) {
                    v.setEstado("ESPERANDO_CONFIRMACION");
                    ventaRepo.save(v);
                }
            });
        }

        redirectAttrs.addFlashAttribute("success",
                "Tu pago está pendiente de confirmación por el administrador.");

        return "redirect:/ventas/mis_compras";
    }

    // 4️⃣ Confirmación de pago por admin y actualización total gastado
    @PostMapping("/admin/confirmar-pago")
    public String adminConfirmaPago(@RequestParam Long ventaId) {

        Venta v = ventaRepo.findById(ventaId).orElse(null);
        if (v == null) return "redirect:/ventas/listar";

        v.setEstado("PAGADO");
        ventaRepo.save(v);

        Usuario u = v.getUsuario();
        double totalVenta = v.getCantidad() * v.getProducto().getPrecio();
        u.setTotalGastado(u.getTotalGastado() + totalVenta);
        usuarioRepo.save(u);

        return "redirect:/ventas/listar";
    }


    // ------------------------------------------
    //   ADMIN VENDER EN NOMBRE DE OTRO USUARIO
    // ------------------------------------------
    @GetMapping("/admin/vender-para")
    public String venderParaForm(Model model, HttpSession session) {

        if (session.getAttribute("userId") == null) return "redirect:/login";

        Object roleObj = session.getAttribute("userRole");
        if (roleObj == null || !"ADMIN".equals(roleObj.toString())) {
            return "redirect:/productos";
        }

        model.addAttribute("productos", productoRepo.findAll());
        model.addAttribute("usuarios", usuarioRepo.findAll());
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", roleObj.toString());

        return "ventas/admin_vender_para";
    }

    @PostMapping("/admin/vender-para")
    public String venderPara(@RequestParam("productoId") Long productoId,
                             @RequestParam("cantidad") Integer cantidad,
                             @RequestParam("usuarioId") Long usuarioId,
                             HttpSession session) {

        if (session.getAttribute("userId") == null) return "redirect:/login";
        Object roleObj = session.getAttribute("userRole");

        if (roleObj == null || !"ADMIN".equals(roleObj.toString())) {
            return "redirect:/productos";
        }

        Usuario u = usuarioRepo.findById(usuarioId).orElse(null);
        Producto p = productoRepo.findById(productoId).orElse(null);

        if (u == null || p == null) return "redirect:/ventas/admin/vender-para";

        Venta v = new Venta();
        v.setUsuario(u);
        v.setProducto(p);
        v.setCantidad(cantidad);
        ventaRepo.save(v);

        return "redirect:/ventas/admin/vender-para";
    }

    @GetMapping("/admin/eliminar/{id}")
    public String eliminarVenta(@PathVariable("id") Long id, HttpSession session) {
        Object roleObj = session.getAttribute("userRole");
        if (roleObj == null || !"ADMIN".equals(roleObj.toString())) {
            return "redirect:/productos";
        }

        ventaRepo.deleteById(id);
        return "redirect:/ventas/listar";
    }

    @GetMapping("/admin/editar/{id}")
    public String editarVentaForm(@PathVariable("id") Long id, Model model, HttpSession session) {
        Object roleObj = session.getAttribute("userRole");
        if (roleObj == null || !"ADMIN".equals(roleObj.toString())) {
            return "redirect:/productos";
        }

        Venta v = ventaRepo.findById(id).orElse(null);
        if (v == null) return "redirect:/ventas/listar";

        model.addAttribute("venta", v);
        model.addAttribute("productos", productoRepo.findAll());
        model.addAttribute("usuarios", usuarioRepo.findAll());
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", roleObj.toString());

        return "ventas/admin_editar_venta";
    }

    @PostMapping("/admin/editar/{id}")
    public String editarVenta(@PathVariable("id") Long id,
                              @RequestParam("usuarioId") Long usuarioId,
                              @RequestParam("productoId") Long productoId,
                              @RequestParam("cantidad") Integer cantidad,
                              HttpSession session) {

        Object roleObj = session.getAttribute("userRole");
        if (roleObj == null || !"ADMIN".equals(roleObj.toString())) {
            return "redirect:/productos";
        }

        Venta v = ventaRepo.findById(id).orElse(null);
        Usuario u = usuarioRepo.findById(usuarioId).orElse(null);
        Producto p = productoRepo.findById(productoId).orElse(null);

        if (v == null || u == null || p == null) return "redirect:/ventas/listar";

        v.setUsuario(u);
        v.setProducto(p);
        v.setCantidad(cantidad);
        ventaRepo.save(v);

        return "redirect:/ventas/listar";
    }

    // ------------------------------------------
    // ⭐ PASO 6 – CAMBIAR ESTADO (ADMIN)
    // ------------------------------------------
@PostMapping("/admin/cambiar-estado")
public String cambiarEstado(@RequestParam Long ventaId,
                            @RequestParam String estado,
                            HttpSession session) {

    Object roleObj = session.getAttribute("userRole");
    if (roleObj == null || !"ADMIN".equals(roleObj.toString())) {
        return "redirect:/productos";
    }

    Venta v = ventaRepo.findById(ventaId).orElse(null);
    if (v != null) {

        // Si pasa a PAGADO por primera vez → sumar al total del usuario
        boolean antesNoEstabaPagado = !"PAGADO".equals(v.getEstado());
        boolean ahoraEsPagado = "PAGADO".equals(estado);

        v.setEstado(estado);
        ventaRepo.save(v);

        if (antesNoEstabaPagado && ahoraEsPagado) {
            Usuario u = v.getUsuario();
            double totalVenta = v.getCantidad() * v.getProducto().getPrecio();
            u.setTotalGastado(u.getTotalGastado() + totalVenta);
            usuarioRepo.save(u);
        }
    }

    return "redirect:/ventas/listar";
}


}


