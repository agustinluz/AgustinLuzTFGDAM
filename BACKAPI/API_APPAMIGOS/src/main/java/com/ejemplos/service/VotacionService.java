package com.ejemplos.service;

import com.ejemplos.modelo.Votacion;
import com.ejemplos.modelo.VotacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VotacionService {

    @Autowired
    private VotacionRepository votacionRepository;

    public Votacion crear(Votacion votacion) {
        return votacionRepository.save(votacion);
    }

    public List<Votacion> obtenerPorGrupo(Long grupoId) {
        return votacionRepository.findByGrupoId(grupoId);
    }
}
