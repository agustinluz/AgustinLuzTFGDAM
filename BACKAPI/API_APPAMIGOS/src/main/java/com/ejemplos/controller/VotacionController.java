package com.ejemplos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ejemplos.DTO.VotacionCreateDTO;
import com.ejemplos.DTO.VotacionDTO;
import com.ejemplos.DTO.VotacionDTOConverter;
import com.ejemplos.DTO.VotacionUpdateDTO;
import com.ejemplos.DTO.VotoCreateDTO;
import com.ejemplos.DTO.VotoDTO;
import com.ejemplos.modelo.Grupo;
import com.ejemplos.modelo.Usuario;
import com.ejemplos.modelo.Votacion;
import com.ejemplos.modelo.Votacion.EstadoVotacion;
import com.ejemplos.modelo.Voto;
import com.ejemplos.modelo.VotacionRepository;
import com.ejemplos.modelo.VotoRepository;
import com.ejemplos.security.JwtUtil;
import com.ejemplos.service.GrupoService;
import com.ejemplos.service.UsuarioService;
import com.ejemplos.service.VotacionService;
import com.ejemplos.service.VotoService;
import com.ejemplos.service.UsuarioGrupoService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8100")
public class VotacionController {

    @Autowired
    private VotacionService votacionService;

    @Autowired
    private VotoService votoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private UsuarioGrupoService usuarioGrupoService;

    @Autowired
    private VotacionDTOConverter votacionDTOConverter;

    @Autowired
    private VotacionRepository votacionRepository;

    @Autowired
    private VotoRepository votoRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // ============ ENDPOINTS DE VOTACIONES ============

    // Obtener votación por ID
    @GetMapping("/votaciones/{id}")
    public ResponseEntity<VotacionDTO> obtenerVotacion(@PathVariable Long id) {
        try {
            Optional<Votacion> votacion = votacionService.obtenerPorId(id);
            if (votacion.isPresent()) {
                VotacionDTO dto = votacionDTOConverter.convertToDTO(votacion.get());
                return ResponseEntity.ok(dto);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Listar votaciones de un grupo (ya existe en GrupoController, pero lo duplico aquí)
    @GetMapping("/grupos/{grupoId}/votaciones")
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

    // Crear votación (ya existe en GrupoController, pero lo mejoro aquí)
    @PostMapping("/grupos/{grupoId}/votaciones")
    public ResponseEntity<VotacionDTO> crearVotacion(
            @PathVariable Long grupoId,
            @RequestBody VotacionCreateDTO votacionDTO,
            @RequestHeader("Authorization") String token) {
        try {
            // Obtener usuario del token
            String jwt = token.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(jwt);
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Verificar que el grupo existe
            Grupo grupo = grupoService.obtenerPorId(grupoId).orElse(null);
            if (grupo == null) {
                return ResponseEntity.notFound().build();
            }

            // Verificar que el usuario pertenece al grupo
            boolean perteneceAlGrupo = usuarioGrupoService.usuarioPerteneceAlGrupo(usuario.getId(), grupoId);
            if (!perteneceAlGrupo) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Validar opciones (mínimo 2)
            if (votacionDTO.getOpciones() == null || votacionDTO.getOpciones().size() < 2) {
                return ResponseEntity.badRequest().build();
            }

            // Crear votación
            Votacion votacion = votacionDTOConverter.convertToEntity(votacionDTO);
            votacion.setGrupo(grupo);
            votacion.setCreador(usuario);
            votacion.setFechaCreacion(new Date());
            votacion.setEstado(EstadoVotacion.ACTIVA);

            Votacion guardada = votacionService.crear(votacion);
            VotacionDTO response = votacionDTOConverter.convertToDTO(guardada);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Actualizar votación
    @PutMapping("/votaciones/{id}")
    public ResponseEntity<VotacionDTO> actualizarVotacion(
            @PathVariable Long id,
            @RequestBody VotacionUpdateDTO votacionDTO,
            @RequestHeader("Authorization") String token) {
        try {
            // Obtener usuario del token
            String jwt = token.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(jwt);
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Obtener votación
            Votacion votacion = votacionService.obtenerPorId(id).orElse(null);
            if (votacion == null) {
                return ResponseEntity.notFound().build();
            }

            // Verificar que es el creador
            if (!votacion.getCreador().getId().equals(usuario.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Verificar que la votación está activa
            if (!"ACTIVA".equals(votacion.getEstado())) {
                return ResponseEntity.badRequest().build();
            }

            // Validar opciones (mínimo 2)
            if (votacionDTO.getOpciones() == null || votacionDTO.getOpciones().size() < 2) {
                return ResponseEntity.badRequest().build();
            }

            // Actualizar campos
            if (votacionDTO.getTitulo() != null) {
                votacion.setTitulo(votacionDTO.getTitulo());
            }
            if (votacionDTO.getDescripcion() != null) {
                votacion.setDescripcion(votacionDTO.getDescripcion());
            }
            if (votacionDTO.getOpciones() != null) {
                votacion.setOpciones(votacionDTO.getOpciones());
            }

            Votacion actualizada = votacionService.actualizar(votacion);
            VotacionDTO response = votacionDTOConverter.convertToDTO(actualizada);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Cerrar votación
    @PutMapping("/votaciones/{id}/cerrar")
    public ResponseEntity<VotacionDTO> cerrarVotacion(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            // Obtener usuario del token
            String jwt = token.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(jwt);
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Obtener votación
            Votacion votacion = votacionService.obtenerPorId(id).orElse(null);
            if (votacion == null) {
                return ResponseEntity.notFound().build();
            }

            // Verificar que es el creador
            if (!votacion.getCreador().getId().equals(usuario.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Verificar que la votación está activa
            if (!"ACTIVA".equals(votacion.getEstado())) {
                return ResponseEntity.badRequest().build();
            }

            // Cerrar votación
            votacion.setEstado(EstadoVotacion.CERRADA);
            votacion.setFechaCierre(new Date());

            Votacion cerrada = votacionService.actualizar(votacion);
            VotacionDTO response = votacionDTOConverter.convertToDTO(cerrada);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Eliminar votación
    @DeleteMapping("/votaciones/{id}")
    public ResponseEntity<Void> eliminarVotacion(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            // Obtener usuario del token
            String jwt = token.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(jwt);
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Obtener votación
            Votacion votacion = votacionService.obtenerPorId(id).orElse(null);
            if (votacion == null) {
                return ResponseEntity.notFound().build();
            }

            // Verificar que es el creador
            if (!votacion.getCreador().getId().equals(usuario.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Eliminar votos asociados primero
            votoRepository.deleteByVotacionId(id);
            
            // Eliminar votación
            votacionService.eliminar(id);

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ============ ENDPOINTS DE VOTOS ============

    // Votar en una votación
    @PostMapping("/votaciones/{id}/votar")
    public ResponseEntity<VotoDTO> votar(
            @PathVariable Long id,
            @RequestBody VotoCreateDTO votoDTO,
            @RequestHeader("Authorization") String token) {
        try {
            // Obtener usuario del token
            String jwt = token.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(jwt);
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Obtener votación
            Votacion votacion = votacionService.obtenerPorId(id).orElse(null);
            if (votacion == null) {
                return ResponseEntity.notFound().build();
            }

            // Verificar que la votación está activa
            if (!"ACTIVA".equals(votacion.getEstado())) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar que el usuario pertenece al grupo
            boolean perteneceAlGrupo = usuarioGrupoService.usuarioPerteneceAlGrupo(
                usuario.getId(), votacion.getGrupo().getId());
            if (!perteneceAlGrupo) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Verificar que no ha votado ya
            Optional<Voto> votoExistente = votoRepository.findByVotacionIdAndUsuarioId(id, usuario.getId());
            if (votoExistente.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build(); // Ya votó
            }

            // Validar que la opción existe
            if (!votacion.getOpciones().contains(votoDTO.getOpcion())) {
                return ResponseEntity.badRequest().build();
            }

            // Crear voto
            Voto voto = new Voto();
            voto.setVotacion(votacion);
            voto.setUsuario(usuario);
            voto.setOpcion(votoDTO.getOpcion());
            voto.setFechaVoto(new Date());

            Voto guardado = votoService.crear(voto);

            VotoDTO response = new VotoDTO();
            response.setId(guardado.getId());
            response.setVotacionId(guardado.getVotacion().getId());
            response.setUsuarioId(guardado.getUsuario().getId());
            response.setOpcion(guardado.getOpcion());
            response.setFechaVoto(guardado.getFechaVoto());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Obtener resultados de una votación
    @GetMapping("/votaciones/{id}/resultados")
    public ResponseEntity<Map<String, Object>> obtenerResultados(@PathVariable Long id) {
        try {
            Votacion votacion = votacionService.obtenerPorId(id).orElse(null);
            if (votacion == null) {
                return ResponseEntity.notFound().build();
            }

            List<Voto> votos = votoRepository.findByVotacionId(id);
            
            // Contar votos por opción
            Map<String, Long> conteoVotos = votos.stream()
                .collect(Collectors.groupingBy(Voto::getOpcion, Collectors.counting()));

            // Agregar opciones sin votos
            for (String opcion : votacion.getOpciones()) {
                conteoVotos.putIfAbsent(opcion, 0L);
            }

            Map<String, Object> resultados = new HashMap<>();
            resultados.put("votacionId", id);
            resultados.put("titulo", votacion.getTitulo());
            resultados.put("estado", votacion.getEstado());
            resultados.put("totalVotos", votos.size());
            resultados.put("resultados", conteoVotos);
            resultados.put("fechaCreacion", votacion.getFechaCreacion());
            resultados.put("fechaCierre", votacion.getFechaCierre());

            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Obtener mi voto en una votación
    @GetMapping("/votaciones/{id}/mi-voto")
    public ResponseEntity<VotoDTO> obtenerMiVoto(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            // Obtener usuario del token
            String jwt = token.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(jwt);
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Optional<Voto> voto = votoRepository.findByVotacionIdAndUsuarioId(id, usuario.getId());
            
            if (voto.isPresent()) {
                VotoDTO response = new VotoDTO();
                response.setId(voto.get().getId());
                response.setVotacionId(voto.get().getVotacion().getId());
                response.setUsuarioId(voto.get().getUsuario().getId());
                response.setOpcion(voto.get().getOpcion());
                response.setFechaVoto(voto.get().getFechaVoto());
                
                return ResponseEntity.ok(response);
            }
            
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}