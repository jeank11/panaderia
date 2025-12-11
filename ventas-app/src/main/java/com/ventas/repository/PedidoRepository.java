package com.ventas.repository;

import com.ventas.model.Pedido;
import com.ventas.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuario(Usuario usuario);

    // Agregás este:
    List<Pedido> findByUsuarioId(Long usuarioId);

    List<Pedido> findByDireccionContainingIgnoreCase(String direccion);

    List<Pedido> findByProductoNombreContainingIgnoreCase(String nombre);

    List<Pedido> findByUsuarioNombreContainingIgnoreCase(String nombre);

    @Query("SELECT p.producto.nombre, SUM(p.cantidad) FROM Pedido p GROUP BY p.producto.nombre")
    List<Object[]> cantidadPedidosPorProducto();

    @Query("SELECT p.producto.nombre, p.fechaEntrega, SUM(p.cantidad) " +
       "FROM Pedido p " +
       "WHERE p.fechaEntrega BETWEEN :fechaInicio AND :fechaFin " +
       "GROUP BY p.producto.nombre, p.fechaEntrega " +
       "ORDER BY p.fechaEntrega, p.producto.nombre")
    List<Object[]> cantidadPedidosPorProductoYFechaEntrega(LocalDate fechaInicio, LocalDate fechaFin);

}


