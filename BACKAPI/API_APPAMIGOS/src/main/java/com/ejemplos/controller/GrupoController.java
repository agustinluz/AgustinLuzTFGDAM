package com.ejemplos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // Obtener grupo por ID
    @GetMapping("/{id}")
    public ResponseEntity<Grupo> obtenerGrupo(@PathVariable Long id) {
        return grupoService.obtenerPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // Crear nuevo grupo
    @PostMapping
    public ResponseEntity<Grupo> crearGrupo(@RequestBody Grupo grupo) {
        return ResponseEntity.ok(grupoService.crear(grupo));
    }

    // Listar usuarios de un grupo
    @GetMapping("/{id}/usuarios")
    public List<Usuario> listarUsuariosGrupo(@PathVariable Long id) {
        return usuarioService.obtenerTodos().stream()
                .filter(u -> u.getGrupo() != null && u.getGrupo().getId().equals(id))
                .collect(Collectors.toList());
    }

    // Registrar usuario en un grupo específico
    @PostMapping("/{id}/usuarios")
    public ResponseEntity<Usuario> registrarUsuarioEnGrupo(@PathVariable Long id, @RequestBody Usuario usuario) {
        Grupo grupo = grupoService.obtenerPorId(id).orElse(null);
        if (grupo == null) return ResponseEntity.notFound().build();
        usuario.setGrupo(grupo);
        return ResponseEntity.ok(usuarioService.crear(usuario));
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
