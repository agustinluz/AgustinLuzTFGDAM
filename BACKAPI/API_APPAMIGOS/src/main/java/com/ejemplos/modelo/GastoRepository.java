package com.ejemplos.modelo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GastoRepository extends JpaRepository<Gasto, Long> {
	List<Gasto> findByGrupoId(Long grupoId);
	
}
