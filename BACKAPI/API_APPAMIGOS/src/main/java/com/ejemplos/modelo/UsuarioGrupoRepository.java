package com.ejemplos.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioGrupoRepository extends JpaRepository<UsuarioGrupo, Long> {
    List<UsuarioGrupo> findByUsuarioId(Long usuarioId);
    List<UsuarioGrupo> findByGrupoId(Long grupoId);
    Optional<UsuarioGrupo> findByUsuarioIdAndGrupoId(Long usuarioId, Long grupoId);
    boolean existsByUsuarioIdAndGrupoId(Long usuarioId, Long grupoId);
    UsuarioGrupo obtenerPorUsuarioIdYGrupoId(Long usuarioId, Long grupoId);
    int contarParticipantesPorGrupo(Long grupoId);
    int contarAdminsPorGrupo(Long grupoId);
    
    int countByGrupoId(Long grupoId);
    int countByGrupoIdAndRol(Long grupoId, String rol);
    
    @Modifying
    @Query("DELETE FROM UsuarioGrupo ug WHERE ug.usuario.id = :usuarioId AND ug.grupo.id = :grupoId")
    void deleteByUsuarioIdAndGrupoId(@Param("usuarioId") Long usuarioId, @Param("grupoId") Long grupoId);
}

