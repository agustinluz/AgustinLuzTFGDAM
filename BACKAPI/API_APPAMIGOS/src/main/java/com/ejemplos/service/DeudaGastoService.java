package com.ejemplos.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ejemplos.modelo.DeudaGasto;
import com.ejemplos.modelo.DeudaGastoRepository;
import com.ejemplos.modelo.Gasto;
import com.ejemplos.modelo.Usuario;

@Service
public class DeudaGastoService {
    
    @Autowired
    private DeudaGastoRepository deudaGastoRepository;
    
    // Crear deudas automáticamente cuando se crea un gasto
    public void crearDeudasParaGasto(Gasto gasto) {
        BigDecimal montoPorPersona = gasto.getMonto()
            .divide(BigDecimal.valueOf(gasto.getUsuarios().size()), 2, RoundingMode.HALF_UP);
        
        Usuario pagador = gasto.getPagadoPor();
        
        for (Usuario participante : gasto.getUsuarios()) {
            // No crear deuda para quien pagó (a menos que quieras manejar casos especiales)
            if (!participante.getId().equals(pagador.getId())) {
                DeudaGasto deuda = new DeudaGasto();
                deuda.setGasto(gasto);
                deuda.setDeudor(participante);
                deuda.setAcreedor(pagador);
                deuda.setMonto(montoPorPersona);
                deuda.setSaldado(false);
                deuda.setFechaCreacion(new Date());
                
                deudaGastoRepository.save(deuda);
            }
        }
    }
    
    // Marcar una deuda como saldada
    public boolean marcarComoSaldado(Long gastoId, Long deudorId, String metodoPago, String notas) {
        // Buscar la deuda específica
        List<DeudaGasto> deudas = deudaGastoRepository.findByGastoId(gastoId);
        
        Optional<DeudaGasto> deudaOpt = deudas.stream()
            .filter(d -> d.getDeudor().getId().equals(deudorId) && !d.isSaldado())
            .findFirst();
            
        if (deudaOpt.isPresent()) {
            DeudaGasto deuda = deudaOpt.get();
            deuda.setSaldado(true);
            deuda.setFechaSaldado(new Date());
            deuda.setMetodoPago(metodoPago);
            deuda.setNotas(notas);
            
            deudaGastoRepository.save(deuda);
            return true;
        }
        
        return false;
    }
    
    // Obtener deudas pendientes de un usuario
    public List<DeudaGasto> obtenerDeudasPendientes(Long deudorId) {
        return deudaGastoRepository.findByDeudorId(deudorId)
            .stream()
            .filter(d -> !d.isSaldado())
            .toList();
    }
    
    // Obtener créditos pendientes de un usuario
    public List<DeudaGasto> obtenerCreditosPendientes(Long acreedorId) {
        return deudaGastoRepository.findByAcreedorId(acreedorId)
            .stream()
            .filter(d -> !d.isSaldado())
            .toList();
    }
    
    // Obtener deudas de un gasto específico
    public List<DeudaGasto> obtenerDeudasPorGasto(Long gastoId) {
        return deudaGastoRepository.findByGastoId(gastoId);
    }
    
    // Actualizar deudas cuando se modifica un gasto
    public void actualizarDeudasParaGasto(Gasto gasto) {
        // Eliminar deudas existentes del gasto
        List<DeudaGasto> deudasExistentes = deudaGastoRepository.findByGastoId(gasto.getId());
        deudaGastoRepository.deleteAll(deudasExistentes);
        
        // Crear nuevas deudas
        crearDeudasParaGasto(gasto);
    }
    
    // Eliminar deudas cuando se elimina un gasto
    public void eliminarDeudasPorGasto(Long gastoId) {
        List<DeudaGasto> deudas = deudaGastoRepository.findByGastoId(gastoId);
        deudaGastoRepository.deleteAll(deudas);
    }
}