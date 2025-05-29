package com.ejemplos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ejemplos.DTO.Evento.EventoCreateDTO;
import com.ejemplos.DTO.Evento.EventoDTO;
import com.ejemplos.DTO.Evento.EventoDTOConverter;
import com.ejemplos.modelo.Evento;
import com.ejemplos.modelo.Grupo;
import com.ejemplos.modelo.Usuario;
import com.ejemplos.security.JwtUtil;
import com.ejemplos.service.EventoService;
import com.ejemplos.service.GrupoService;
import com.ejemplos.service.UsuarioService;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    @Autowired
    private EventoService eventoService;

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EventoDTOConverter eventoDTOConverter;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/{grupoId}/crear")
    public ResponseEntity<EventoDTO> crearEventoEnGrupo(
            @PathVariable Long grupoId,
            @RequestBody EventoCreateDTO dto,
            @RequestHeader("Authorization") String token) {
        try {
            // Validar que el grupo existe
            Grupo grupo = grupoService.obtenerPorId(grupoId).orElse(null);
            if (grupo == null) {
                return ResponseEntity.notFound().build();
            }

            // Validar token y usuario
            String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Crear evento usando el converter
            Evento evento = eventoDTOConverter.convertToEntity(dto);
            evento.setGrupo(grupo);
            
            Evento eventoGuardado = eventoService.crear(evento);
            EventoDTO response = eventoDTOConverter.convertToDTO(eventoGuardado);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    
    
    @GetMapping("/{grupoId}/eventos")
    public ResponseEntity<List<Evento>> listarEventosGrupo(@PathVariable Long grupoId) {
        List<Evento> eventos = eventoService.obtenerPorGrupo(grupoId);
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/{eventoId}")
    public ResponseEntity<EventoDTO> obtenerEvento(@PathVariable Long eventoId) {
        return eventoService.obtenerPorId(eventoId)
                .map(eventoDTOConverter::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{eventoId}")
    public ResponseEntity<EventoDTO> actualizarEvento(
            @PathVariable Long eventoId,
            @RequestBody EventoCreateDTO dto,
            @RequestHeader("Authorization") String token) {
        try {
            // Validar token y usuario
            String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Evento eventoExistente = eventoService.obtenerPorId(eventoId).orElse(null);
            if (eventoExistente == null) {
                return ResponseEntity.notFound().build();
            }

            // Actualizar campos
            eventoExistente.setTitulo(dto.getTitulo());
            eventoExistente.setDescripcion(dto.getDescripcion());
            eventoExistente.setFecha(dto.getFecha());
            eventoExistente.setUbicacion(dto.getUbicacion());

            Evento actualizado = eventoService.actualizar(eventoExistente);
            EventoDTO response = eventoDTOConverter.convertToDTO(actualizado);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{eventoId}")
    public ResponseEntity<Void> eliminarEvento(
            @PathVariable Long eventoId,
            @RequestHeader("Authorization") String token) {
        try {
            // Validar token y usuario
            String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            if (!eventoService.existePorId(eventoId)) {
                return ResponseEntity.notFound().build();
            }

            eventoService.eliminar(eventoId);
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}