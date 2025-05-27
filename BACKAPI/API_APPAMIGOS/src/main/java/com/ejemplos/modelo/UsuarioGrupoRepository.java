package com.ejemplos.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioGrupoRepository extends JpaRepository<UsuarioGrupo, Long> {
    List<UsuarioGrupo> findByUsuarioId(Long usuarioId);
    List<UsuarioGrupo> findByGrupoId(Long grupoId);
    Optional<UsuarioGrupo> findByUsuarioIdAndGrupoId(Long usuarioId, Long grupoId);
    boolean existsByUsuarioIdAndGrupoId(Long usuarioId, Long grupoId);
}

