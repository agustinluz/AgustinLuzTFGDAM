package com.ejemplos.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ejemplos.modelo.Evento;
import com.ejemplos.modelo.EventoRepository;

import java.util.List;

@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;

    public List<Evento> obtenerPorGrupo(Long grupoId) {
        return eventoRepository.findByGrupoId(grupoId);
    }

    public Evento crear(Evento evento) {
        return eventoRepository.save(evento);
    }
}
