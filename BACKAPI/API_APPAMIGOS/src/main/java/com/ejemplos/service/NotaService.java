package com.ejemplos.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ejemplos.modelo.NotaRepository;

import com.ejemplos.modelo.Nota;
@Service
public class NotaService {

	@Autowired
	private NotaRepository notaRepository;
	
	public List<Nota> obtenerPorGrupo(Long grupoId) {
	    return notaRepository.findByGrupoId(grupoId);
	}
	public Nota crear(Nota nota) {
	    return notaRepository.save(nota);
	}


}
