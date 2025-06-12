package com.ejemplos.controller;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ejemplos.DTO.Grupo.GrupoDTO;
import com.ejemplos.DTO.Grupo.GrupoDTOConverter;
import com.ejemplos.DTO.Usuario.UsuarioCreateDTO;
import com.ejemplos.DTO.Usuario.UsuarioDTO;
import com.ejemplos.DTO.Usuario.UsuarioLoginDTO;
import com.ejemplos.modelo.Usuario;
import com.ejemplos.security.JwtUtil;
import com.ejemplos.service.GrupoService;
import com.ejemplos.service.UsuarioService;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private GrupoService grupoService;
    
    @Autowired
    private GrupoDTOConverter grupoDTOConverter;

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
    public ResponseEntity<?> registrarUsuario(@RequestBody UsuarioCreateDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(dto.getPassword());

        try {
            Usuario creado = usuarioService.crear(usuario);

            UsuarioDTO response = new UsuarioDTO();
            response.setId(creado.getId());
            response.setNombre(creado.getNombre());
            response.setEmail(creado.getEmail());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/invitacion/{codigo}")
    public ResponseEntity<GrupoDTO> obtenerPorCodigoInvitacion(@PathVariable String codigo) {
        return grupoService.obtenerPorCodigoInvitacion(codigo)
                .map(grupoDTOConverter::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
}