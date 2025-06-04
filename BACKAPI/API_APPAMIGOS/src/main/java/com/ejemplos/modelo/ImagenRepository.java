package com.ejemplos.modelo;

import com.ejemplos.modelo.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImagenRepository extends JpaRepository<Imagen, Long> {
    
    // Spring Data JPA creará automáticamente la consulta basándose en el nombre del método
    List<Imagen> findByGrupoIdOrderByFechaCreacionDesc(Long grupoId);
    
    // Métodos adicionales útiles
    List<Imagen> findByGrupoId(Long grupoId);
    
    List<Imagen> findByUsuarioId(Long usuarioId);
    
    List<Imagen> findByEventoId(Long eventoId);
    
    // Para contar imágenes por grupo
    long countByGrupoId(Long grupoId);
    
    // Para obtener las últimas N imágenes de un grupo
    List<Imagen> findTop10ByGrupoIdOrderByFechaCreacionDesc(Long grupoId);
}