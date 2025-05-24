package com.ejemplos.modelo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VotacionRepository extends JpaRepository<Votacion, Long> {
	List<Votacion> findByGrupoId(Long grupoId);
}
