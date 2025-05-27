package com.ejemplos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ejemplos.DTO.GastoCreateDTO;
import com.ejemplos.DTO.Usuario.UsuarioDTO;
import com.ejemplos.modelo.DeudaGasto;
import com.ejemplos.modelo.Evento;
import com.ejemplos.modelo.Gasto;
import com.ejemplos.modelo.Grupo;
import com.ejemplos.modelo.Usuario;
import com.ejemplos.service.DeudaGastoService;
import com.ejemplos.service.EventoService;
import com.ejemplos.service.GastoService;
import com.ejemplos.service.GrupoService;
import com.ejemplos.service.UsuarioService;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gasto")
@CrossOrigin(origins = "http://localhost:8100")
public class GastoController {

    @Autowired
    private GastoService gastoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private EventoService eventoService;

    @Autowired
    private DeudaGastoService deudaGastoService;

    @PostMapping("/{grupoId}/crear")
    public ResponseEntity<Gasto> crearGasto(
            @PathVariable Long grupoId,
            @RequestBody GastoCreateDTO gastoDTO) {
        
        Grupo grupo = grupoService.obtenerPorId(grupoId).orElse(null);
        if (grupo == null) return ResponseEntity.notFound().build();

        Usuario pagadoPor = usuarioService.obtenerPorId(gastoDTO.getPagadoPorId()).orElse(null);
        if (pagadoPor == null) return ResponseEntity.badRequest().build();

        Gasto gasto = new Gasto();
        gasto.setTitulo(gastoDTO.getTitulo());
        gasto.setMonto(gastoDTO.getMonto());
        gasto.setGrupo(grupo);
        gasto.setPagadoPor(pagadoPor);

        // Asociar a evento si se ha indicado
        if (gastoDTO.getEventoId() != null) {
            Evento evento = eventoService.obtenerPorId(gastoDTO.getEventoId()).orElse(null);
            if (evento != null) {
                gasto.setEvento(evento);
            }
        }

        // Guardar participantes
        List<Usuario> participantes = usuarioService.obtenerPorIds(gastoDTO.getParticipantesIds());
        gasto.setUsuarios(participantes);

        // Guardar gasto
        Gasto guardado = gastoService.crear(gasto);
        
        // Crear deudas automáticamente
        deudaGastoService.crearDeudasParaGasto(guardado);

        return ResponseEntity.ok(guardado);
    }
    
    

    @GetMapping("/{grupoId}/gastos")
    public ResponseEntity<List<Gasto>> listarGastosGrupo(@PathVariable Long grupoId) {
        Grupo grupo = grupoService.obtenerPorId(grupoId).orElse(null);
        if (grupo == null) return ResponseEntity.notFound().build();
        
        List<Gasto> gastos = gastoService.obtenerPorGrupo(grupoId);
        return ResponseEntity.ok(gastos);
    }

    @GetMapping("/{gastoId}")
    public ResponseEntity<Gasto> obtenerGasto(@PathVariable Long gastoId) {
        return gastoService.obtenerPorId(gastoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{gastoId}")
    public ResponseEntity<Gasto> actualizarGasto(
            @PathVariable Long gastoId,
            @RequestBody GastoCreateDTO gastoDTO) {
        
        Optional<Gasto> gastoExistente = gastoService.obtenerPorId(gastoId);
        if (gastoExistente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Gasto gasto = gastoExistente.get();
        gasto.setTitulo(gastoDTO.getTitulo());
        gasto.setMonto(gastoDTO.getMonto());

        // Actualizar pagador si cambió
        Usuario pagadoPor = usuarioService.obtenerPorId(gastoDTO.getPagadoPorId()).orElse(null);
        if (pagadoPor != null) {
            gasto.setPagadoPor(pagadoPor);
        }

        // Actualizar participantes
        List<Usuario> participantes = usuarioService.obtenerPorIds(gastoDTO.getParticipantesIds());
        gasto.setUsuarios(participantes);

        // Actualizar evento si cambió
        if (gastoDTO.getEventoId() != null) {
            Evento evento = eventoService.obtenerPorId(gastoDTO.getEventoId()).orElse(null);
            gasto.setEvento(evento);
        } else {
            gasto.setEvento(null);
        }

        Gasto actualizado = gastoService.actualizar(gasto);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{gastoId}")
    public ResponseEntity<Void> eliminarGasto(@PathVariable Long gastoId) {
        if (!gastoService.existePorId(gastoId)) {
            return ResponseEntity.notFound().build();
        }
        
        gastoService.eliminar(gastoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("grupos/{gastoId}/participantes")
    public ResponseEntity<List<UsuarioDTO>> obtenerParticipantesGasto(@PathVariable Long gastoId) {
        Optional<Gasto> gastoOpt = gastoService.obtenerPorId(gastoId);
        if (gastoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<UsuarioDTO> participantesDTO = gastoOpt.get().getUsuarios().stream().map(u -> {
            UsuarioDTO dto = new UsuarioDTO();
            dto.setId(u.getId());
            dto.setNombre(u.getNombre());
            dto.setEmail(u.getEmail());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(participantesDTO);
    }

    @PostMapping("grupos/{gastoId}/participantes/{participanteId}/saldado")
    public ResponseEntity<String> marcarComoSaldado(
            @PathVariable Long gastoId,
            @PathVariable Long participanteId,
            @RequestBody(required = false) Map<String, String> body) {
        
        // Verificar que el gasto existe
        Optional<Gasto> gastoOpt = gastoService.obtenerPorId(gastoId);
        if (gastoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String metodoPago = body != null ? body.get("metodoPago") : "no especificado";
        String notas = body != null ? body.get("notas") : "";
        
        boolean saldado = deudaGastoService.marcarComoSaldado(gastoId, participanteId, metodoPago, notas);
        
        if (saldado) {
            return ResponseEntity.ok("Marcado como saldado correctamente");
        } else {
            return ResponseEntity.badRequest().body("No se encontró la deuda o ya estaba saldada");
        }
    }

    @GetMapping("/grupos/{grupoId}/eventos/{eventoId}")
    public ResponseEntity<List<Gasto>> obtenerGastosPorEvento(
            @PathVariable Long grupoId, 
            @PathVariable Long eventoId) {
        
        List<Gasto> gastos = gastoService.obtenerPorGrupoYEvento(grupoId, eventoId);
        return ResponseEntity.ok(gastos);
    }

    @GetMapping("/grupos/{grupoId}/deudas")
    public ResponseEntity<Map<String, Object>> obtenerResumenDeudas(@PathVariable Long grupoId) {
        List<Gasto> gastos = gastoService.obtenerPorGrupo(grupoId);
        
        Map<String, BigDecimal> balances = new HashMap<>();
        
        for (Gasto gasto : gastos) {
            String pagador = gasto.getPagadoPor().getNombre();
            BigDecimal montoPorPersona = gasto.getMonto().divide(
                BigDecimal.valueOf(gasto.getUsuarios().size()), 
                2, 
                BigDecimal.ROUND_HALF_UP
            );
            
            // El pagador tiene saldo positivo
            balances.put(pagador, balances.getOrDefault(pagador, BigDecimal.ZERO).add(
                gasto.getMonto().subtract(montoPorPersona)
            ));
            
            // Los participantes tienen saldo negativo
            for (Usuario participante : gasto.getUsuarios()) {
                if (!participante.getId().equals(gasto.getPagadoPor().getId())) {
                    balances.put(participante.getNombre(), 
                        balances.getOrDefault(participante.getNombre(), BigDecimal.ZERO)
                            .subtract(montoPorPersona)
                    );
                }
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("balances", balances);
        response.put("totalGastos", gastos.stream()
            .map(Gasto::getMonto)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{gastoId}/deudas")
    public ResponseEntity<List<DeudaGasto>> obtenerDeudasGasto(@PathVariable Long gastoId) {
        List<DeudaGasto> deudas = deudaGastoService.obtenerDeudasPorGasto(gastoId);
        return ResponseEntity.ok(deudas);
    }
}