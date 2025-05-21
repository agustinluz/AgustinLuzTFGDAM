package com.ejemplos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ejemplos.DTO.EventoCreateDTO;
import com.ejemplos.DTO.GastoCreateDTO;
import com.ejemplos.DTO.GrupoCreateDTO;
import com.ejemplos.DTO.GrupoDTO;
import com.ejemplos.DTO.GrupoDTOConverter;
import com.ejemplos.DTO.UsuarioCreateDTO;
import com.ejemplos.DTO.UsuarioDTO;
import com.ejemplos.DTO.UsuarioLoginDTO;
import com.ejemplos.modelo.Evento;
import com.ejemplos.modelo.Gasto;
import com.ejemplos.modelo.Grupo;
import com.ejemplos.modelo.Usuario;
import com.ejemplos.security.JwtUtil;
import com.ejemplos.service.EventoService;
import com.ejemplos.service.GastoService;
import com.ejemplos.service.GrupoService;
import com.ejemplos.service.UsuarioService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grupos")
@CrossOrigin(origins = "http://localhost:8100")
public class GrupoController {

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EventoService eventoService;
    
    @Autowired
    private GastoService gastoService;
    
    @Autowired
    private GrupoDTOConverter grupoDTOConverter;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UsuarioLoginDTO loginDTO) {
        Optional<Usuario> usuarioOpt = usuarioService.login(loginDTO.getEmail(), loginDTO.getPassword());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }

        Usuario usuario = usuarioOpt.get();
        String token = jwtUtil.generateToken(usuario.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);

        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(usuario.getId());
        usuarioDTO.setNombre(usuario.getNombre());
        usuarioDTO.setEmail(usuario.getEmail());

        
        if (usuario.getGrupo() != null) {
            usuarioDTO.setGrupoId(usuario.getGrupo().getId());
        }

        response.put("usuario", usuarioDTO);

        return ResponseEntity.ok(response);
    }

    
    @PostMapping("/registro")
    public ResponseEntity<UsuarioDTO> registrarUsuario(@RequestBody UsuarioCreateDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(dto.getPassword());

        Usuario creado = usuarioService.crear(usuario);

        UsuarioDTO response = new UsuarioDTO();
        response.setId(creado.getId());
        response.setNombre(creado.getNombre());
        response.setEmail(creado.getEmail());

        return ResponseEntity.ok(response);
    }
    @GetMapping("/invitacion/{codigo}")
    public ResponseEntity<GrupoDTO> obtenerPorCodigoInvitacion(@PathVariable String codigo) {
        return grupoService.obtenerPorCodigoInvitacion(codigo)
                .map(grupoDTOConverter::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



    
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


    @PostMapping("/{id}/usuarios")
    public ResponseEntity<UsuarioDTO> registrarUsuarioEnGrupo(
            @PathVariable Long id,
            @RequestBody UsuarioCreateDTO usuarioDTO
    ) {
        Grupo grupo = grupoService.obtenerPorId(id).orElse(null);
        if (grupo == null) return ResponseEntity.notFound().build();

        // Buscar usuario por email
        Optional<Usuario> existente = usuarioService.obtenerPorEmail(usuarioDTO.getEmail());

        Usuario usuario;
        if (existente.isPresent()) {
            // Si ya existe, actualizamos su grupo
            usuario = existente.get();
            usuario.setGrupo(grupo);
            // Opcional: podrías actualizar también el nombre, pero **nunca la contraseña** aquí
        } else {
            // Si no existe, lo creamos
            usuario = new Usuario();
            usuario.setNombre(usuarioDTO.getNombre());
            usuario.setEmail(usuarioDTO.getEmail());
            usuario.setPassword(usuarioDTO.getPassword());
            usuario.setGrupo(grupo);
        }

        Usuario guardado = usuarioService.crear(usuario);

        UsuarioDTO response = new UsuarioDTO();
        response.setId(guardado.getId());
        response.setNombre(guardado.getNombre());
        response.setEmail(guardado.getEmail());

        return ResponseEntity.ok(response);
    }

    
    @PostMapping("/grupos/{grupoId}/gastos")
    public ResponseEntity<Gasto> crearGasto(
        @PathVariable Long grupoId,
        @RequestBody GastoCreateDTO gastoDTO
    ) {
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

        return ResponseEntity.ok(guardado);
    }

    @PostMapping("/api/usuarios")
    public ResponseEntity<UsuarioDTO> crearUsuario(@RequestBody UsuarioCreateDTO usuarioDTO) {
        Usuario usuario = new Usuario();
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setPassword(usuarioDTO.getPassword());

        Usuario creado = usuarioService.crear(usuario);

        UsuarioDTO response = new UsuarioDTO();
        response.setId(creado.getId());
        response.setNombre(creado.getNombre());
        response.setEmail(creado.getEmail());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/eventos")
    public ResponseEntity<Evento> crearEventoEnGrupo(@PathVariable Long id, @RequestBody EventoCreateDTO dto) {
        Grupo grupo = grupoService.obtenerPorId(id).orElse(null);
        if (grupo == null) return ResponseEntity.notFound().build();

        Evento evento = new Evento();
        evento.setGrupo(grupo);
        evento.setTitulo(dto.getTitulo());
        evento.setDescripcion(dto.getDescripcion());
        evento.setUbicacion(dto.getUbicacion());
        evento.setFecha(dto.getFecha());

        return ResponseEntity.ok(eventoService.crear(evento));
    }


    // Listar eventos de un grupo
    @GetMapping("/{id}/eventos")
    public List<Evento> listarEventosGrupo(@PathVariable Long id) {
        return eventoService.obtenerPorGrupo(id);
    }


}
