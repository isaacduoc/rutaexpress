package com.duoc.backend.repository;

import com.duoc.backend.entity.Envio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvioRepository
        extends JpaRepository<Envio, Long> {

    /*
     * Todos los envíos que todavía deben
     * aparecer en la pantalla operativa.
     *
     * archivado NULL se considera visible
     * para compatibilidad con registros antiguos.
     */
    @Query("""
        SELECT e
        FROM Envio e
        WHERE e.archivado = false
           OR e.archivado IS NULL
        ORDER BY e.fechaCreacion DESC
        """)
    List<Envio> findVisibles();


    /*
     * Envíos visibles pertenecientes
     * exclusivamente a un remitente.
     */
    @Query("""
        SELECT e
        FROM Envio e
        WHERE LOWER(e.correoRemitente) =
              LOWER(:correo)
          AND (
                e.archivado = false
                OR e.archivado IS NULL
              )
        ORDER BY e.fechaCreacion DESC
        """)
    List<Envio> findVisiblesByCorreoRemitente(
            @Param("correo")
            String correo
    );
}