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
    
    @Autowired
    private DeudaGastoService deudaGastoService;

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

    public boolean existePorId(Long id) {
        return gastoRepository.existsById(id);
    }

    public List<Gasto> obtenerPorGrupoYEvento(Long grupoId, Long eventoId) {
        return gastoRepository.findByGrupoIdAndEventoId(grupoId, eventoId);
    }
    
 // En GastoService, actualizar el método actualizar:
    public Gasto actualizar(Gasto gasto) {
        Gasto actualizado = gastoRepository.save(gasto);
        deudaGastoService.actualizarDeudasParaGasto(actualizado);
        return actualizado;
    }

    // Y el método eliminar:
    public void eliminar(Long id) {
        deudaGastoService.eliminarDeudasPorGasto(id);
        gastoRepository.deleteById(id);
    }
    
    
}
