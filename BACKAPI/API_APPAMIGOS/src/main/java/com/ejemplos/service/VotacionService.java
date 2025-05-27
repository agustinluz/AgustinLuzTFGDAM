package com.ejemplos.service;

import com.ejemplos.modelo.Votacion;
import com.ejemplos.modelo.VotacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    public List<Votacion> obtenerTodas() {
        return votacionRepository.findAll();
    }

    public Optional<Votacion> obtenerPorId(Long id) {
        return votacionRepository.findById(id);
    }

    public List<Votacion> obtenerPorGrupoId(Long grupoId) {
        return votacionRepository.findByGrupoId(grupoId);
    }

    public List<Votacion> obtenerPorCreadorId(Long creadorId) {
        return votacionRepository.findByCreadorId(creadorId);
    }

    public List<Votacion> obtenerActivasPorGrupoId(Long grupoId) {
        return votacionRepository.findByGrupoIdAndEstado(grupoId, Votacion.EstadoVotacion.ACTIVA);
    }

    public Votacion actualizar(Votacion votacion) {
        return votacionRepository.save(votacion);
    }

    public void eliminar(Long id) {
        votacionRepository.deleteById(id);
    }

    public boolean existePorId(Long id) {
        return votacionRepository.existsById(id);
    }

    public long contarPorGrupoId(Long grupoId) {
        return votacionRepository.countByGrupoId(grupoId);
    }

    public long contarActivasPorGrupoId(Long grupoId) {
        return votacionRepository.countByGrupoIdAndEstado(grupoId, Votacion.EstadoVotacion.ACTIVA);
    }
}
