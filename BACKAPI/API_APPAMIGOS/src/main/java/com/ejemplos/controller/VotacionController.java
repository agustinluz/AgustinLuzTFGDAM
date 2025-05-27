package com.ejemplos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ejemplos.DTO.Votacion.VotacionCreateDTO;
import com.ejemplos.DTO.Votacion.VotacionDTO;
import com.ejemplos.DTO.Votacion.VotacionDTOConverter;
import com.ejemplos.DTO.Votacion.VotacionUpdateDTO;
import com.ejemplos.DTO.Votacion.VotoCreateDTO;
import com.ejemplos.DTO.Votacion.VotoDTO;
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

    @GetMapping("/grupos/{grupoId}/votaciones")
    public ResponseEntity<?> listarVotacionesGrupo(
            @PathVariable Long grupoId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            System.out.println("=== LISTAR VOTACIONES ===");
            System.out.println("Buscando votaciones para grupo ID: " + grupoId);
            System.out.println("Token: " + token);

            // Opcional: Validar autenticación para listar
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    String jwt = token.replace("Bearer ", "");
                    String email = jwtUtil.extractEmail(jwt);
                    Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
                    
                    if (usuario != null) {
                        boolean perteneceAlGrupo = usuarioGrupoService.usuarioPerteneceAlGrupo(usuario.getId(), grupoId);
                        if (!perteneceAlGrupo) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("error", "No tienes acceso a las votaciones de este grupo"));
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error al validar token para listar: " + e.getMessage());
                    // Continuar sin autenticación si falla
                }
            }

            List<Votacion> votaciones = votacionRepository.findByGrupoId(grupoId);
            System.out.println("Votaciones encontradas: " + votaciones.size());

            List<VotacionDTO> votacionesDTO = votaciones.stream()
                .map(votacionDTOConverter::convertToDTO)
                .collect(Collectors.toList());

            return ResponseEntity.ok(votacionesDTO);
            
        } catch (Exception e) {
            System.err.println("Error al obtener votaciones: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor"));
        }
    }

    @PostMapping("/grupos/{grupoId}/votaciones")
    public ResponseEntity<?> crearVotacion(
            @PathVariable Long grupoId,
            @RequestBody VotacionCreateDTO votacionDTO,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            System.out.println("=== CREAR VOTACIÓN ===");
            System.out.println("Token recibido: " + token);
            System.out.println("Grupo ID: " + grupoId);
            System.out.println("Datos votación: " + votacionDTO);
            
            // Validar token
            if (token == null || !token.startsWith("Bearer ")) {
                System.out.println("Token inválido o faltante");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token de autorización requerido"));
            }

            // Obtener usuario del token
            String jwt = token.replace("Bearer ", "");
            System.out.println("JWT extraído: " + jwt);
            
            String email;
            try {
                email = jwtUtil.extractEmail(jwt);
                System.out.println("Email extraído del token: " + email);
            } catch (Exception e) {
                System.out.println("Error al extraer email del token: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token inválido"));
            }
            
            Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);
            if (usuario == null) {
                System.out.println("Usuario no encontrado con email: " + email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no encontrado"));
            }
            
            System.out.println("Usuario encontrado: " + usuario.getNombre());

            // Verificar que el grupo existe
            Grupo grupo = grupoService.obtenerPorId(grupoId).orElse(null);
            if (grupo == null) {
                System.out.println("Grupo no encontrado con ID: " + grupoId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Grupo no encontrado"));
            }
            
            System.out.println("Grupo encontrado: " + grupo.getNombre());

            // Verificar que el usuario pertenece al grupo
            boolean perteneceAlGrupo = usuarioGrupoService.usuarioPerteneceAlGrupo(usuario.getId(), grupoId);
            if (!perteneceAlGrupo) {
                System.out.println("Usuario no pertenece al grupo");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No tienes permisos para crear votaciones en este grupo"));
            }
            
            System.out.println("Usuario pertenece al grupo");

            // Validar datos de la votación
            if (votacionDTO.getPregunta() == null || votacionDTO.getPregunta().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "La pregunta es obligatoria"));
            }
            
            if (votacionDTO.getOpciones() == null || votacionDTO.getOpciones().size() < 2) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Se requieren al menos 2 opciones"));
            }
            
            // Validar que las opciones no estén vacías
            List<String> opcionesValidas = votacionDTO.getOpciones().stream()
                .filter(opcion -> opcion != null && !opcion.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());
                
            if (opcionesValidas.size() < 2) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Se requieren al menos 2 opciones válidas"));
            }

            // Crear votación
            Votacion votacion = new Votacion();
            votacion.setTitulo(votacionDTO.getPregunta().trim());
            votacion.setDescripcion(votacionDTO.getDescripcion()); // Si existe en el DTO
            votacion.setOpciones(opcionesValidas);
            votacion.setGrupo(grupo);
            votacion.setCreador(usuario);
            votacion.setFechaCreacion(new Date());
            votacion.setEstado(Votacion.EstadoVotacion.ACTIVA);
            
            // Establecer fecha límite si se proporcionó
            if (votacionDTO.getFechaLimite() != null) {
                votacion.setFechaCierre(votacionDTO.getFechaLimite());
            }

            System.out.println("Guardando votación...");
            Votacion guardada = votacionService.crear(votacion);
            System.out.println("Votación guardada con ID: " + guardada.getId());
            
            VotacionDTO response = votacionDTOConverter.convertToDTO(guardada);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error al crear votación: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
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