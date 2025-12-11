package com.ventas.repository;

import com.ventas.model.Venta;
import com.ventas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    // --- Métodos existentes ---
    List<Venta> findByUsuario(Usuario usuario);

    List<Venta> findByUsuarioId(Long usuarioId);

    // --- Nuevos métodos necesarios para el sistema de pagos ---

    // 1) Para mostrar en "Mis compras": todas las ventas NO PAGADAS
    List<Venta> findByUsuarioIdAndEstadoNot(Long usuarioId, String estado);

    // 2) Para el panel del administrador:
    List<Venta> findByEstado(String estado);

    // 3) Para permitir pagos parciales:
    List<Venta> findByIdInAndUsuarioId(List<Long> ids, Long usuarioId);
}


