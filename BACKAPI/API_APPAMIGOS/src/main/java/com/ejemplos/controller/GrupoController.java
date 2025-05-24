package com.ejemplos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ejemplos.DTO.EventoCreateDTO;
import com.ejemplos.DTO.EventoDTO;
import com.ejemplos.DTO.EventoDTOConverter;
import com.ejemplos.DTO.GastoCreateDTO;
import com.ejemplos.DTO.GrupoCreateDTO;
import com.ejemplos.DTO.GrupoDTO;
import com.ejemplos.DTO.GrupoDTOConverter;
import com.ejemplos.DTO.NotaCreateDTO;
import com.ejemplos.DTO.NotaDTO;
import com.ejemplos.DTO.NotaDTOConverter;
import com.ejemplos.DTO.UsuarioCreateDTO;
import com.ejemplos.DTO.UsuarioDTO;
import com.ejemplos.DTO.UsuarioLoginDTO;
import com.ejemplos.DTO.VotacionCreateDTO;
import com.ejemplos.DTO.VotacionDTO;
import com.ejemplos.DTO.VotacionDTOConverter;
import com.ejemplos.modelo.Evento;
import com.ejemplos.modelo.Gasto;
import com.ejemplos.modelo.Grupo;
import com.ejemplos.modelo.Imagen;
import com.ejemplos.modelo.Nota;
import com.ejemplos.modelo.NotaRepository;
import com.ejemplos.modelo.Usuario;
import com.ejemplos.modelo.UsuarioGrupo;
import com.ejemplos.modelo.Votacion;
import com.ejemplos.modelo.VotacionRepository;
import com.ejemplos.security.JwtUtil;
import com.ejemplos.service.EventoService;
import com.ejemplos.service.GastoService;
import com.ejemplos.service.GrupoService;
import com.ejemplos.service.ImagenService;
import com.ejemplos.service.NotaService;
import com.ejemplos.service.UsuarioGrupoService;
import com.ejemplos.service.UsuarioService;
import com.ejemplos.service.VotacionService;

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

    
    @Autowired
    private UsuarioGrupoService usuarioGrupoService;
    
    @Autowired
    private NotaService notaService;

    @Autowired
    private VotacionService votacionService;
    
    @Autowired
    private ImagenService imagenService;
    
    @Autowired
    private NotaRepository notaRepository;
    
    @Autowired
    private VotacionRepository votacionRepository;
    
    @Autowired
    private NotaDTOConverter notaDTOConverter;

    @Autowired
    private VotacionDTOConverter votacionDTOConverter;
    
    @Autowired
    private EventoDTOConverter eventoDTOConverter;

    
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

        // Obtener grupos del usuario desde la entidad UsuarioGrupo
        List<Long> gruposIds = usuario.getUsuarioGrupos().stream()
            .map(ug -> ug.getGrupo().getId())
            .toList();

        usuarioDTO.setGrupoIds(gruposIds);

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

    
    @GetMapping("/usuarios/{id}/grupos")
    public ResponseEntity<List<GrupoDTO>> obtenerGruposDeUsuario(@PathVariable Long id) {
        List<UsuarioGrupo> asociaciones = usuarioGrupoService.obtenerPorUsuarioId(id);
        List<GrupoDTO> gruposDTO = asociaciones.stream()
                .map(UsuarioGrupo::getGrupo)
                .map(grupoDTOConverter::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(gruposDTO);
    }



    // Crear nuevo grupo
    @PostMapping
    public ResponseEntity<GrupoDTO> crearGrupo(@RequestBody GrupoCreateDTO grupoCreateDTO) {
        Grupo grupo = grupoDTOConverter.convertToEntity(grupoCreateDTO);
        Grupo grupoGuardado = grupoService.crear(grupo);
        return ResponseEntity.ok(grupoDTOConverter.convertToDTO(grupoGuardado));
    }


    @GetMapping("/{id}/usuarios")
    public ResponseEntity<List<UsuarioDTO>> listarUsuariosGrupo(@PathVariable Long id) {
        List<Usuario> usuarios = usuarioGrupoService.obtenerUsuariosPorGrupoId(id);

        List<UsuarioDTO> usuariosDTO = usuarios.stream().map(u -> {
            UsuarioDTO dto = new UsuarioDTO();
            dto.setId(u.getId());
            dto.setNombre(u.getNombre());
            dto.setEmail(u.getEmail());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(usuariosDTO);
    }

    
    


    @PostMapping("/{id}/usuarios")
    public ResponseEntity<UsuarioDTO> registrarUsuarioEnGrupo(
            @PathVariable Long id,
            @RequestBody UsuarioCreateDTO usuarioDTO
    ) {
        Grupo grupo = grupoService.obtenerPorId(id).orElse(null);
        if (grupo == null) return ResponseEntity.notFound().build();

        Optional<Usuario> existente = usuarioService.obtenerPorEmail(usuarioDTO.getEmail());
        Usuario usuario;

        if (existente.isPresent()) {
            usuario = existente.get();
        } else {
            usuario = new Usuario();
            usuario.setNombre(usuarioDTO.getNombre());
            usuario.setEmail(usuarioDTO.getEmail());
            usuario.setPassword(usuarioDTO.getPassword());
            usuario = usuarioService.crear(usuario);
        }

        // Registrar en la tabla intermedia
        UsuarioGrupo usuarioGrupo = new UsuarioGrupo();
        usuarioGrupo.setUsuario(usuario);
        usuarioGrupo.setGrupo(grupo);
        usuarioGrupo.setRol("miembro");

        usuarioGrupoService.guardar(usuarioGrupo);

        UsuarioDTO response = new UsuarioDTO();
        response.setId(usuario.getId());
        response.setNombre(usuario.getNombre());
        response.setEmail(usuario.getEmail());

        return ResponseEntity.ok(response);
    }


    
    @PostMapping("{grupoId}/gastos")
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

    @PostMapping("/{grupoId}/eventos")
    public ResponseEntity<EventoDTO> crearEventoEnGrupo(
            @PathVariable Long grupoId, // Cambié 'id' por 'grupoId' para que coincida
            @RequestBody EventoCreateDTO dto,
            @RequestHeader("Authorization") String token
    ) {
        try {
            // Validar que el grupo existe
            Grupo grupo = grupoService.obtenerPorId(grupoId).orElse(null);
            if (grupo == null) {
                return ResponseEntity.notFound().build();
            }

            // Validar token y usuario (opcional, según tu lógica de negocio)
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


    // Listar eventos de un grupo
    @GetMapping("/{id}/eventos")
    public List<Evento> listarEventosGrupo(@PathVariable Long id) {
        return eventoService.obtenerPorGrupo(id);
    }
    
    @GetMapping("/{id}/gastos")
    public ResponseEntity<List<Gasto>> listarGastosGrupo(@PathVariable Long id) {
        Grupo grupo = grupoService.obtenerPorId(id).orElse(null);
        if (grupo == null) return ResponseEntity.notFound().build();
        List<Gasto> gastos = gastoService.obtenerPorGrupo(id);
        return ResponseEntity.ok(gastos);
    }

    
    @GetMapping("/{grupoId}/votaciones")
    public ResponseEntity<List<VotacionDTO>> listarVotacionesGrupo(@PathVariable Long grupoId) {
        try {
            List<Votacion> votaciones = votacionRepository.findByGrupoId(grupoId);
            List<VotacionDTO> votacionesDTO = votaciones.stream()
                    .map(votacionDTOConverter::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(votacionesDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{grupoId}/notas")
    public ResponseEntity<List<NotaDTO>> listarNotasGrupo(@PathVariable Long grupoId) {
        try {
            List<Nota> notas = notaRepository.findByGrupoId(grupoId);
            List<NotaDTO> notasDTO = notas.stream()
                    .map(notaDTOConverter::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(notasDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @PostMapping("/{grupoId}/notas")
    public ResponseEntity<NotaDTO> crearNota(
            @PathVariable Long grupoId,
            @RequestBody NotaCreateDTO notaDTO,
            @RequestHeader("Authorization") String token
    ) {
        try {
            // Validar que el grupo existe
            Grupo grupo = grupoService.obtenerPorId(grupoId).orElse(null);
            if (grupo == null) {
                return ResponseEntity.notFound().build();
            }

            // Extraer y validar usuario del token
            String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Crear la nota
            Nota nota = notaDTOConverter.convertToEntity(notaDTO);
            nota.setGrupo(grupo);
            nota.setUsuario(usuario);

            Nota guardada = notaService.crear(nota);
            NotaDTO response = notaDTOConverter.convertToDTO(guardada);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace(); // Para debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @PostMapping("/{grupoId}/votaciones")
    public ResponseEntity<VotacionDTO> crearVotacion(
        @PathVariable Long grupoId, 
        @RequestBody VotacionCreateDTO votacionDTO,
        @RequestHeader("Authorization") String token
    ) {
        try {
            // Verificar que el grupo existe
            Grupo grupo = grupoService.obtenerPorId(grupoId).orElse(null);
            if (grupo == null) {
                return ResponseEntity.notFound().build();
            }

            // Validar opciones
            if (votacionDTO.getOpciones() == null || votacionDTO.getOpciones().size() < 2) {
                return ResponseEntity.badRequest().build();
            }

            // Crear votación
            Votacion votacion = votacionDTOConverter.convertToEntity(votacionDTO);
            votacion.setGrupo(grupo);
            votacion.setFechaCreacion(new Date());

            Votacion guardada = votacionService.crear(votacion);
            VotacionDTO response = votacionDTOConverter.convertToDTO(guardada);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
