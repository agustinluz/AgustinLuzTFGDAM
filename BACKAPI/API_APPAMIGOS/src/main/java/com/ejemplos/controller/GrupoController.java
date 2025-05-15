package com.ejemplos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ejemplos.DTO.GrupoCreateDTO;
import com.ejemplos.DTO.GrupoDTO;
import com.ejemplos.DTO.GrupoDTOConverter;
import com.ejemplos.DTO.UsuarioCreateDTO;
import com.ejemplos.DTO.UsuarioDTO;
import com.ejemplos.modelo.Evento;
import com.ejemplos.modelo.Grupo;
import com.ejemplos.modelo.Usuario;

import Service.EventoService;
import Service.GrupoService;
import Service.UsuarioService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grupos")
@CrossOrigin(origins = "*")
public class GrupoController {

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EventoService eventoService;
    
    @Autowired
    private GrupoDTOConverter grupoDTOConverter;


    // Obtener grupo por ID
    @GetMapping("/{id}")
    public ResponseEntity<GrupoDTO> obtenerGrupo(@PathVariable Long id) {
        return grupoService.obtenerPorId(id)
                .map(grupoDTOConverter::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // Crear nuevo grupo
    @PostMapping
    public ResponseEntity<GrupoDTO> crearGrupo(@RequestBody GrupoCreateDTO grupoCreateDTO) {
        Grupo grupo = grupoDTOConverter.convertToEntity(grupoCreateDTO);
        Grupo grupoGuardado = grupoService.crear(grupo);
        return ResponseEntity.ok(grupoDTOConverter.convertToDTO(grupoGuardado));
    }


    // Listar usuarios de un grupo
    @GetMapping("/{id}/usuarios")
    public ResponseEntity<List<UsuarioDTO>> listarUsuariosGrupo(@PathVariable Long id) {
        List<UsuarioDTO> usuariosDTO = usuarioService.obtenerTodos().stream()
                .filter(u -> u.getGrupo() != null && u.getGrupo().getId().equals(id))
                .map(u -> {
                    UsuarioDTO dto = new UsuarioDTO();
                    dto.setId(u.getId());
                    dto.setNombre(u.getNombre());
                    dto.setEmail(u.getEmail());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(usuariosDTO);
    }


    // Registrar usuario en un grupo específico
    @PostMapping("/{id}/usuarios")
    public ResponseEntity<UsuarioDTO> registrarUsuarioEnGrupo(
            @PathVariable Long id,
            @RequestBody UsuarioCreateDTO usuarioDTO
    ) {
        Grupo grupo = grupoService.obtenerPorId(id).orElse(null);
        if (grupo == null) return ResponseEntity.notFound().build();

        Usuario usuario = new Usuario();
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setPassword(usuarioDTO.getPassword());
        usuario.setGrupo(grupo);

        Usuario creado = usuarioService.crear(usuario);

        UsuarioDTO response = new UsuarioDTO();
        response.setId(creado.getId());
        response.setNombre(creado.getNombre());
        response.setEmail(creado.getEmail());

        return ResponseEntity.ok(response);
    }


    // Listar eventos de un grupo
    @GetMapping("/{id}/eventos")
    public List<Evento> listarEventosGrupo(@PathVariable Long id) {
        return eventoService.obtenerPorGrupo(id);
    }

    // Crear evento en grupo
    @PostMapping("/{id}/eventos")
    public ResponseEntity<Evento> crearEventoEnGrupo(@PathVariable Long id, @RequestBody Evento evento) {
        Grupo grupo = grupoService.obtenerPorId(id).orElse(null);
        if (grupo == null) return ResponseEntity.notFound().build();
        evento.setGrupo(grupo);
        return ResponseEntity.ok(eventoService.crear(evento));
    }
}
