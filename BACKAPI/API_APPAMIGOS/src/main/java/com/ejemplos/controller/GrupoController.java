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


    
    
    
    


}
