package com.sivil.systeam.repository;

import com.sivil.systeam.entity.CompraOnline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompraOnlineRepository extends JpaRepository<CompraOnline, Integer> {

    @Query("""
           select c
           from CompraOnline c
           join fetch c.comprador comp
           order by c.fecha_compra desc
           """)
    List<CompraOnline> findAllWithComprador();

    @Query("""
           select c
           from CompraOnline c
           join fetch c.comprador comp
           left join fetch c.detallesCompra detalles
           left join fetch detalles.libro
           where comp.id_usuario = :usuarioId
           order by c.fecha_compra desc
           """)
    List<CompraOnline> findByCompradorIdOrderByFechaDesc(@Param("usuarioId") Integer usuarioId);

    @Query("""
           select c
           from CompraOnline c
           join fetch c.comprador comp
           where c.numero_orden = :numero
           """)
    Optional<CompraOnline> findByNumeroOrden(@Param("numero") String numero);
}
