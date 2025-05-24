package com.ejemplos.modelo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImagenRepository extends JpaRepository<Imagen, Long> {
	List<Imagen> findByGrupoId(Long grupoId);

}
