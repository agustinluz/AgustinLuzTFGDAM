package com.ejemplos.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ejemplos.modelo.Imagen;
import com.ejemplos.modelo.ImagenRepository;
import com.ejemplos.modelo.Votacion;

@Service
public class ImagenService {

	@Autowired
	private ImagenRepository imagenRepository;
	
	public List<Imagen> obtenerPorGrupo(Long grupoId) {
	    return imagenRepository.findByGrupoId(grupoId);
	}
}
