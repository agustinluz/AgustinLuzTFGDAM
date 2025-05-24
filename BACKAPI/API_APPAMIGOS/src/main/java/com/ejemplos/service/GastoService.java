package com.ejemplos.service;


import com.ejemplos.modelo.Gasto;
import com.ejemplos.modelo.GastoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GastoService {

    @Autowired
    private GastoRepository gastoRepository;

    public Gasto crear(Gasto gasto) {
        return gastoRepository.save(gasto);
    }
    
    public List<Gasto> obtenerPorGrupo(Long grupoId) {
        return gastoRepository.findByGrupoId(grupoId);
    }


    public Optional<Gasto> obtenerPorId(Long id) {
        return gastoRepository.findById(id);
    }

    public List<Gasto> obtenerTodos() {
        return gastoRepository.findAll();
    }

    public void eliminar(Long id) {
        gastoRepository.deleteById(id);
    }
    
    
}
