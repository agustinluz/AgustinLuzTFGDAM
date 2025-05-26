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
import com.ejemplos.modelo.DeudaGasto;
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
import com.ejemplos.service.DeudaGastoService;
import com.ejemplos.service.EventoService;
import com.ejemplos.service.GastoService;
import com.ejemplos.service.GrupoService;
import com.ejemplos.service.ImagenService;
import com.ejemplos.service.NotaService;
import com.ejemplos.service.UsuarioGrupoService;
import com.ejemplos.service.UsuarioService;
import com.ejemplos.service.VotacionService;

import java.math.BigDecimal;
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
    private GrupoDTOConverter grupoDTOConverter;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioGrupoService usuarioGrupoService;
    
    @Autowired
    private VotacionService votacionService;
    
    @Autowired
    private ImagenService imagenService;
    
    @Autowired
    private VotacionRepository votacionRepository;
    

    @Autowired
    private VotacionDTOConverter votacionDTOConverter;
    
    
    // Obtener grupo por ID
    @GetMapping("/{id}")
    public ResponseEntity<GrupoDTO> obtenerGrupo(@PathVariable Long id) {
        return grupoService.obtenerPorId(id)
                .map(grupoDTOConverter::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    
    @GetMapping("/usuario/{id}")
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

    
    
    
    
    @GetMapping("/{grupoId}/participantes")
    public ResponseEntity<List<UsuarioDTO>> obtenerParticipantesGrupo(@PathVariable Long grupoId) {
        List<Usuario> usuarios = usuarioGrupoService.obtenerUsuariosPorGrupoId(grupoId);

        List<UsuarioDTO> usuariosDTO = usuarios.stream().map(u -> {
            UsuarioDTO dto = new UsuarioDTO();
            dto.setId(u.getId());
            dto.setNombre(u.getNombre());
            dto.setEmail(u.getEmail());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(usuariosDTO);
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
