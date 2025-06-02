package com.ejemplos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ejemplos.DTO.Evento.EventoCreateDTO;
import com.ejemplos.DTO.Evento.EventoDTO;
import com.ejemplos.DTO.Evento.EventoDTOConverter;
import com.ejemplos.DTO.Gasto.GastoCreateDTO;
import com.ejemplos.DTO.Grupo.GrupoCreateDTO;
import com.ejemplos.DTO.Grupo.GrupoDTO;
import com.ejemplos.DTO.Grupo.GrupoDTOConverter;
import com.ejemplos.DTO.Nota.NotaCreateDTO;
import com.ejemplos.DTO.Nota.NotaDTO;
import com.ejemplos.DTO.Nota.NotaDTOConverter;
import com.ejemplos.DTO.Usuario.UsuarioCreateDTO;
import com.ejemplos.DTO.Usuario.UsuarioDTO;
import com.ejemplos.DTO.Usuario.UsuarioLoginDTO;
import com.ejemplos.DTO.Votacion.VotacionCreateDTO;
import com.ejemplos.DTO.Votacion.VotacionDTO;
import com.ejemplos.DTO.Votacion.VotacionDTOConverter;
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

 // Crear nuevo grupo con usuario como admin
 @PostMapping
 public ResponseEntity<GrupoDTO> crearGrupo(@RequestBody GrupoCreateDTO grupoCreateDTO, @RequestHeader("usuarioId") Long usuarioId) {
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

 
//Actualizar grupo (solo admin)
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


    @PutMapping("/{grupoId}/usuarios/{usuarioId}/rol")
    public ResponseEntity<Void> cambiarRolUsuario(
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
        UsuarioGrupo usuarioGrupo = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(usuarioId, grupoId);
        if (usuarioGrupo == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Si se está asignando como admin, verificar que el usuario existe
        if (nuevoRol.equals("admin")) {
            Usuario nuevoAdmin = usuarioService.obtenerPorId(usuarioId).orElse(null);
            if (nuevoAdmin == null) {
                return ResponseEntity.notFound().build();
            }
        }
        
        // Actualizar el rol
        usuarioGrupo.setRol(nuevoRol);
        usuarioGrupoService.guardar(usuarioGrupo);
        
        return ResponseEntity.ok().build();
    }

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
    
    
    
    
    @PutMapping("/{grupoId}/transferir-admin")
    public ResponseEntity<Void> transferirAdministracion(
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
        
        UsuarioGrupo usuarioGrupoNuevo = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(nuevoAdminId, grupoId);
        if (usuarioGrupoNuevo == null) {
            return ResponseEntity.badRequest().build(); // El usuario no es miembro del grupo
        }
        
        // Cambiar el admin del grupo
        grupo.setAdmin(nuevoAdmin);
        grupoService.crear(grupo);
        
        // Actualizar roles en la tabla intermedia
        usuarioGrupoNuevo.setRol("admin");
        usuarioGrupoService.guardar(usuarioGrupoNuevo);
        
        // Cambiar el rol del admin anterior a miembro
        UsuarioGrupo adminAnterior = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(adminActualId, grupoId);
        if (adminAnterior != null) {
            adminAnterior.setRol("miembro");
            usuarioGrupoService.guardar(adminAnterior);
        }
        
        return ResponseEntity.ok().build();
    }
    
    
    
    @DeleteMapping("/{grupoId}/salir")
    public ResponseEntity<Void> salirDelGrupo(
            @PathVariable Long grupoId,
            @RequestHeader("usuarioId") Long usuarioId) {
        
        Grupo grupo = grupoService.obtenerPorId(grupoId).orElse(null);
        if (grupo == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Verificar que el usuario es miembro del grupo
        UsuarioGrupo usuarioGrupo = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(usuarioId, grupoId);
        if (usuarioGrupo == null) {
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
    
    
    
    @GetMapping("/{grupoId}/usuarios/{usuarioId}")
    public ResponseEntity<UsuarioDTO> obtenerParticipante(
            @PathVariable Long grupoId,
            @PathVariable Long usuarioId,
            @RequestHeader("solicitanteId") Long solicitanteId) {
        
        // Verificar que el solicitante es miembro del grupo
        UsuarioGrupo solicitante = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(solicitanteId, grupoId);
        if (solicitante == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Obtener la información del participante
        UsuarioGrupo participante = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(usuarioId, grupoId);
        if (participante == null) {
            return ResponseEntity.notFound().build();
        }
        
        Usuario usuario = participante.getUsuario();
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        // Agregar el rol al DTO si es necesario
        
        return ResponseEntity.ok(dto);
    }

    
    
    
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
    
    
    @GetMapping("/{id}/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasGrupo(
            @PathVariable Long id,
            @RequestHeader("usuarioId") Long usuarioId) {
        
        // Verificar que el usuario es miembro del grupo
        UsuarioGrupo miembro = usuarioGrupoService.obtenerPorUsuarioIdYGrupoId(usuarioId, id);
        if (miembro == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Grupo grupo = grupoService.obtenerPorId(id).orElse(null);
        if (grupo == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Contar participantes
        int totalParticipantes = usuarioGrupoService.contarParticipantesPorGrupo(id);
        estadisticas.put("totalParticipantes", totalParticipantes);
        
        // Contar admins
        int totalAdmins = usuarioGrupoService.contarAdminsPorGrupo(id);
        estadisticas.put("totalAdmins", totalAdmins);
        
        // Fecha de creación (si tienes este campo)
        // estadisticas.put("fechaCreacion", grupo.getFechaCreacion());
        
        // Otros contadores si tienes las relaciones
        if (grupo.getEventos() != null) {
            estadisticas.put("totalEventos", grupo.getEventos().size());
        }
        if (grupo.getGastos() != null) {
            estadisticas.put("totalGastos", grupo.getGastos().size());
        }
        if (grupo.getNotas() != null) {
            estadisticas.put("totalNotas", grupo.getNotas().size());
        }
        
        return ResponseEntity.ok(estadisticas);
    }
    
    
    
    


}
