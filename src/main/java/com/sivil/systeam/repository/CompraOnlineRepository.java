package com.sivil.systeam.repository;

import com.sivil.systeam.entity.CompraOnline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface CompraOnlineRepository extends JpaRepository<CompraOnline, Integer>{

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


    @Query("""
SELECT c
FROM CompraOnline c
WHERE c.comprador.id_usuario = :idUsuario
""")
    List<CompraOnline> findAllByComprador(@Param("idUsuario") Integer idUsuario, Sort sort);




    @Query("""
       SELECT c
       FROM CompraOnline c
       WHERE c.comprador.id_usuario = :usuarioId
         AND UPPER(c.numero_orden) LIKE CONCAT('%', UPPER(:numero), '%')
       """)
    List<CompraOnline> findAllByCompradorAndNumero(@Param("usuarioId") Integer usuarioId,
                                                   @Param("numero") String numero,
                                                   Sort sort);
}

