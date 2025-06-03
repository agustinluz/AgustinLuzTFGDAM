package com.ejemplos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ejemplos.DTO.Grupo.GrupoCreateDTO;
import com.ejemplos.DTO.Grupo.GrupoDTO;
import com.ejemplos.DTO.Grupo.GrupoDTOConverter;
import com.ejemplos.DTO.Usuario.UsuarioCreateDTO;
import com.ejemplos.DTO.Usuario.UsuarioDTO;
import com.ejemplos.DTO.Usuario.UsuarioDTOConverter;
import com.ejemplos.DTO.Usuario.UsuarioGrupoDTO;
import com.ejemplos.DTO.Usuario.UsuarioGrupoDTOConverter;
import com.ejemplos.modelo.Grupo;
import com.ejemplos.modelo.Usuario;
import com.ejemplos.modelo.UsuarioGrupo;
import com.ejemplos.security.JwtUtil;
import com.ejemplos.service.GrupoService;
import com.ejemplos.service.UsuarioGrupoService;
import com.ejemplos.service.UsuarioService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grupos")
public class GrupoController {

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private GrupoDTOConverter grupoDTOConverter;

    @Autowired
    private UsuarioDTOConverter usuarioDTOConverter;

    @Autowired
    private UsuarioGrupoDTOConverter usuarioGrupoDTOConverter;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioGrupoService usuarioGrupoService;

    // Obtener grupo por ID
    @GetMapping("/{id}")
    public ResponseEntity<GrupoDTO> obtenerGrupo(@PathVariable Long id) {
        return grupoService.obtenerPorId(id)
                .map(grupoDTOConverter::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener grupos de un usuario
    @GetMapping("/usuario/{id}")
    public ResponseEntity<List<GrupoDTO>> obtenerGruposDeUsuario(@PathVariable Long id) {
        List<UsuarioGrupo> asociaciones = usuarioGrupoService.obtenerPorUsuarioId(id);
        List<GrupoDTO> gruposDTO = asociaciones.stream()
                .map(UsuarioGrupo::getGrupo)
                .map(grupoDTOConverter::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(gruposDTO);
    }

    // Crear nuevo grupo con usuario como admin
    @PostMapping
    public ResponseEntity<GrupoDTO> crearGrupo(@RequestBody GrupoCreateDTO grupoCreateDTO, 
                                               @RequestHeader("usuarioId") Long usuarioId) {
        // Validar que el nombre no esté vacío
        if (grupoCreateDTO.getNombre() == null || grupoCreateDTO.getNombre().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Obtener el usuario que será admin
        Usuario admin = usuarioService.obtenerPorId(usuarioId).orElse(null);
        if (admin == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Crear el grupo
        Grupo grupo = grupoDTOConverter.convertToEntity(grupoCreateDTO);
        grupo.setAdmin(admin);
        Grupo grupoGuardado = grupoService.crear(grupo);
        
        // Asociar el usuario como admin del grupo
        UsuarioGrupo usuarioGrupo = new UsuarioGrupo();
        usuarioGrupo.setUsuario(admin);
        usuarioGrupo.setGrupo(grupoGuardado);
        usuarioGrupo.setRol("admin");
        usuarioGrupoService.guardar(usuarioGrupo);
        
        return ResponseEntity.ok(grupoDTOConverter.convertToDTO(grupoGuardado));
    }

    // Actualizar grupo (solo admin)
    @PutMapping("/{id}")
    public ResponseEntity<GrupoDTO> actualizarGrupo(
            @PathVariable Long id, 
            @RequestBody GrupoCreateDTO grupoUpdateDTO,
            @RequestHeader("usuarioId") Long usuarioId) {
        
        Grupo grupo = grupoService.obtenerPorId(id).orElse(null);
        if (grupo == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Verificar que el usuario es admin del grupo
        if (grupo.getAdmin() == null || !grupo.getAdmin().getId().equals(usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Actualizar datos
        if (grupoUpdateDTO.getNombre() != null && !grupoUpdateDTO.getNombre().trim().isEmpty()) {
            grupo.setNombre(grupoUpdateDTO.getNombre());
        }
        if (grupoUpdateDTO.getImagenPerfil() != null) {
            grupo.setImagenPerfil(grupoUpdateDTO.getImagenPerfil());
        }
        
        Grupo grupoActualizado = grupoService.crear(grupo);
        return ResponseEntity.ok(grupoDTOConverter.convertToDTO(grupoActualizado));
    }

    // Eliminar participante del grupo (solo admin)
    @DeleteMapping("/{grupoId}/usuarios/{usuarioId}")
    public ResponseEntity<Void> eliminarParticipante(
            @PathVariable Long grupoId,
            @PathVariable Long usuarioId,
            @RequestHeader("adminId") Long adminId) {
        
        Grupo grupo = grupoService.obtenerPorId(grupoId).orElse(null);
        if (grupo == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Verificar que el usuario es admin del grupo
        if (grupo.getAdmin() == null || !grupo.getAdmin().getId().equals(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // No permitir que el admin se elimine a sí mismo
        if (usuarioId.equals(adminId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        // Eliminar la asociación usuario-grupo
        usuarioGrupoService.eliminarUsuarioDeGrupo(usuarioId, grupoId);
        
        return ResponseEntity.ok().build();
    }

    // Listar usuarios del grupo (información básica)
    @GetMapping("/{id}/usuarios")
    public ResponseEntity<List<UsuarioDTO>> listarUsuariosGrupo(@PathVariable Long id) {
        List<Usuario> usuarios = usuarioGrupoService.obtenerUsuariosPorGrupoId(id);
        List<UsuarioDTO> usuariosDTO = usuarioDTOConverter.convertToDTOList(usuarios);
        return ResponseEntity.ok(usuariosDTO);
    }

    // Obtener participantes con información básica
    @GetMapping("/{grupoId}/participantes")
    public ResponseEntity<List<UsuarioDTO>> obtenerParticipantesGrupo(
            @PathVariable Long grupoId,
            @RequestHeader("usuarioId") Long usuarioId) {
        
        // Verificar que el usuario es miembro del grupo
        Optional<UsuarioGrupo> solicitante = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(usuarioId, grupoId);
        if (solicitante.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<Usuario> usuarios = usuarioGrupoService.obtenerUsuariosPorGrupoId(grupoId);
        List<UsuarioDTO> usuariosDTO = usuarioDTOConverter.convertToDTOList(usuarios);
        
        return ResponseEntity.ok(usuariosDTO);
    }

    // Obtener participantes con información completa (roles incluidos)
    @GetMapping("/{grupoId}/participantes-con-roles")
    public ResponseEntity<List<UsuarioGrupoDTO>> obtenerParticipantesConRoles(
            @PathVariable Long grupoId,
            @RequestHeader("usuarioId") Long usuarioId) {
        
        // Verificar que el usuario es miembro del grupo
        Optional<UsuarioGrupo> solicitante = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(usuarioId, grupoId);
        if (solicitante.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<UsuarioGrupo> asociaciones = usuarioGrupoService.obtenerPorUsuarioId(grupoId);
        List<UsuarioGrupoDTO> participantesConRoles = usuarioGrupoDTOConverter.convertToDTOList(asociaciones);
        
        return ResponseEntity.ok(participantesConRoles);
    }

    // Registrar usuario en grupo (crear usuario si no existe)
    @PostMapping("/{id}/usuarios")
    public ResponseEntity<UsuarioDTO> registrarUsuarioEnGrupo(
            @PathVariable Long id,
            @RequestBody UsuarioCreateDTO usuarioDTO) {
        
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

        return ResponseEntity.ok(usuarioDTOConverter.convertToDTO(usuario));
    }

    // Buscar usuario por email (para invitaciones)
    @GetMapping("/buscar-usuario")
    public ResponseEntity<UsuarioDTO> buscarUsuarioPorEmail(
            @RequestParam String email,
            @RequestHeader("usuarioId") Long usuarioId) {
        
        Optional<Usuario> usuario = usuarioService.obtenerPorEmail(email);
        if (usuario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        UsuarioDTO dto = usuarioDTOConverter.convertToDTO(usuario.get());
        return ResponseEntity.ok(dto);
    }

    // Invitar usuario existente al grupo
    @PostMapping("/{grupoId}/invitar")
    public ResponseEntity<UsuarioGrupoDTO> invitarUsuarioExistente(
            @PathVariable Long grupoId,
            @RequestParam String emailUsuario,
            @RequestHeader("adminId") Long adminId) {
        
        Grupo grupo = grupoService.obtenerPorId(grupoId).orElse(null);
        if (grupo == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Verificar que el usuario es admin del grupo
        if (grupo.getAdmin() == null || !grupo.getAdmin().getId().equals(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Buscar usuario por email
        Optional<Usuario> usuarioOpt = usuarioService.obtenerPorEmail(emailUsuario);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Usuario usuario = usuarioOpt.get();
        
        // Verificar que no es ya miembro del grupo
        Optional<UsuarioGrupo> existente = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(usuario.getId(), grupoId);
        if (existente.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        // Crear asociación
        UsuarioGrupo usuarioGrupo = new UsuarioGrupo();
        usuarioGrupo.setUsuario(usuario);
        usuarioGrupo.setGrupo(grupo);
        usuarioGrupo.setRol("miembro");
        usuarioGrupoService.guardar(usuarioGrupo);
        
        UsuarioGrupoDTO response = usuarioGrupoDTOConverter.convertToDTO(usuarioGrupo);
        return ResponseEntity.ok(response);
    }

    // Cambiar rol de usuario
    @PutMapping("/{grupoId}/usuarios/{usuarioId}/rol")
    public ResponseEntity<UsuarioGrupoDTO> cambiarRolUsuario(
            @PathVariable Long grupoId,
            @PathVariable Long usuarioId,
            @RequestParam String nuevoRol,
            @RequestHeader("adminId") Long adminId) {
        
        // Validar que el rol sea válido
        if (!nuevoRol.equals("admin") && !nuevoRol.equals("miembro")) {
            return ResponseEntity.badRequest().build();
        }
        
        Grupo grupo = grupoService.obtenerPorId(grupoId).orElse(null);
        if (grupo == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Verificar que el usuario solicitante es admin del grupo
        if (grupo.getAdmin() == null || !grupo.getAdmin().getId().equals(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // No permitir que el admin se quite a sí mismo el rol de admin
        if (usuarioId.equals(adminId) && nuevoRol.equals("miembro")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        // Buscar la asociación usuario-grupo
        Optional<UsuarioGrupo> usuarioGrupoOpt = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(usuarioId, grupoId);
        if (usuarioGrupoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        UsuarioGrupo usuarioGrupo = usuarioGrupoOpt.get();
        
        // Actualizar el rol
        usuarioGrupo.setRol(nuevoRol);
        usuarioGrupoService.guardar(usuarioGrupo);
        
        UsuarioGrupoDTO response = usuarioGrupoDTOConverter.convertToDTO(usuarioGrupo);
        return ResponseEntity.ok(response);
    }

    // Transferir administración
    @PutMapping("/{grupoId}/transferir-admin")
    public ResponseEntity<Map<String, String>> transferirAdministracion(
            @PathVariable Long grupoId,
            @RequestParam Long nuevoAdminId,
            @RequestHeader("adminId") Long adminActualId) {
        
        Grupo grupo = grupoService.obtenerPorId(grupoId).orElse(null);
        if (grupo == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Verificar que el usuario solicitante es admin actual
        if (grupo.getAdmin() == null || !grupo.getAdmin().getId().equals(adminActualId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Verificar que el nuevo admin existe y es miembro del grupo
        Usuario nuevoAdmin = usuarioService.obtenerPorId(nuevoAdminId).orElse(null);
        if (nuevoAdmin == null) {
            return ResponseEntity.notFound().build();
        }
        
        Optional<UsuarioGrupo> usuarioGrupoNuevoOpt = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(nuevoAdminId, grupoId);
        if (usuarioGrupoNuevoOpt.isEmpty()) {
            return ResponseEntity.badRequest().build(); // El usuario no es miembro del grupo
        }
        
        UsuarioGrupo usuarioGrupoNuevo = usuarioGrupoNuevoOpt.get();
        
        // Cambiar el admin del grupo
        grupo.setAdmin(nuevoAdmin);
        grupoService.crear(grupo);
        
        // Actualizar roles en la tabla intermedia
        usuarioGrupoNuevo.setRol("admin");
        usuarioGrupoService.guardar(usuarioGrupoNuevo);
        
        // Cambiar el rol del admin anterior a miembro
        Optional<UsuarioGrupo> adminAnteriorOpt = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(adminActualId, grupoId);
        if (adminAnteriorOpt.isPresent()) {
            UsuarioGrupo adminAnterior = adminAnteriorOpt.get();
            adminAnterior.setRol("miembro");
            usuarioGrupoService.guardar(adminAnterior);
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Administración transferida exitosamente");
        response.put("nuevoAdmin", nuevoAdmin.getNombre());
        
        return ResponseEntity.ok(response);
    }

    // Salir del grupo
    @DeleteMapping("/{grupoId}/salir")
    public ResponseEntity<Void> salirDelGrupo(
            @PathVariable Long grupoId,
            @RequestHeader("usuarioId") Long usuarioId) {
        
        Grupo grupo = grupoService.obtenerPorId(grupoId).orElse(null);
        if (grupo == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Verificar que el usuario es miembro del grupo
        Optional<UsuarioGrupo> usuarioGrupoOpt = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(usuarioId, grupoId);
        if (usuarioGrupoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // No permitir que el admin salga del grupo sin transferir la administración
        if (grupo.getAdmin() != null && grupo.getAdmin().getId().equals(usuarioId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        // Eliminar la asociación usuario-grupo
        usuarioGrupoService.eliminarUsuarioDeGrupo(usuarioId, grupoId);
        
        return ResponseEntity.ok().build();
    }

    // Obtener información de un participante específico
    @GetMapping("/{grupoId}/usuarios/{usuarioId}")
    public ResponseEntity<UsuarioGrupoDTO> obtenerParticipante(
            @PathVariable Long grupoId,
            @PathVariable Long usuarioId,
            @RequestHeader("solicitanteId") Long solicitanteId) {
        
        // Verificar que el solicitante es miembro del grupo
        Optional<UsuarioGrupo> solicitante = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(solicitanteId, grupoId);
        if (solicitante.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Obtener la información del participante
        Optional<UsuarioGrupo> participanteOpt = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(usuarioId, grupoId);
        if (participanteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        UsuarioGrupoDTO dto = usuarioGrupoDTOConverter.convertToDTO(participanteOpt.get());
        return ResponseEntity.ok(dto);
    }

    // Generar nuevo código de invitación
    @PostMapping("/{id}/generar-codigo")
    public ResponseEntity<Map<String, String>> generarNuevoCodigo(
            @PathVariable Long id,
            @RequestHeader("usuarioId") Long usuarioId) {
        
        Grupo grupo = grupoService.obtenerPorId(id).orElse(null);
        if (grupo == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Verificar que el usuario es admin del grupo
        if (grupo.getAdmin() == null || !grupo.getAdmin().getId().equals(usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Generar nuevo código
        String nuevoCodigo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        grupo.setCodigoInvitacion(nuevoCodigo);
        grupoService.crear(grupo);
        
        Map<String, String> response = new HashMap<>();
        response.put("codigoInvitacion", nuevoCodigo);
        
        return ResponseEntity.ok(response);
    }

    // Obtener estadísticas del grupo
    @GetMapping("/{id}/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasGrupo(
            @PathVariable Long id,
            @RequestHeader("usuarioId") Long usuarioId) {
        
        // Verificar que el usuario es miembro del grupo
        Optional<UsuarioGrupo> miembro = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(usuarioId, id);
        if (miembro.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Grupo grupo = grupoService.obtenerPorId(id).orElse(null);
        if (grupo == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Información básica del grupo
        estadisticas.put("grupoId", grupo.getId());
        estadisticas.put("nombreGrupo", grupo.getNombre());
        
        // Contar participantes
        int totalParticipantes = usuarioGrupoService.contarParticipantesPorGrupo(id);
        estadisticas.put("totalParticipantes", totalParticipantes);
        
        // Contar admins y miembros
        int totalAdmins = usuarioGrupoService.contarAdminsPorGrupo(id);
        estadisticas.put("totalAdmins", totalAdmins);
        estadisticas.put("totalMiembros", totalParticipantes - totalAdmins);
        
        // Otros contadores si tienes las relaciones configuradas
        if (grupo.getEventos() != null) {
            estadisticas.put("totalEventos", grupo.getEventos().size());
        } else {
            estadisticas.put("totalEventos", 0);
        }
        
        if (grupo.getGastos() != null) {
            estadisticas.put("totalGastos", grupo.getGastos().size());
        } else {
            estadisticas.put("totalGastos", 0);
        }
        
        if (grupo.getNotas() != null) {
            estadisticas.put("totalNotas", grupo.getNotas().size());
        } else {
            estadisticas.put("totalNotas", 0);
        }
        
        return ResponseEntity.ok(estadisticas);
    }

    // Eliminar grupo (solo admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarGrupo(
            @PathVariable Long id,
            @RequestHeader("usuarioId") Long usuarioId) {
        
        Grupo grupo = grupoService.obtenerPorId(id).orElse(null);
        if (grupo == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Verificar que el usuario es admin del grupo
        if (grupo.getAdmin() == null || !grupo.getAdmin().getId().equals(usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Eliminar el grupo (las asociaciones se eliminan por cascada)
        grupoService.eliminar(id);
        
        return ResponseEntity.ok().build();
    }
}