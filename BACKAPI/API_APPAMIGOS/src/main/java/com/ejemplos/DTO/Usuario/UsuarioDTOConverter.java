package com.ejemplos.DTO.Usuario;

import com.ejemplos.modelo.Usuario;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UsuarioDTOConverter {

    public UsuarioDTO convertToDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        // Agregar otros campos que tenga la entidad Usuario
        
        return dto;
    }

    public List<UsuarioDTO> convertToDTOList(List<Usuario> usuarios) {
        return usuarios.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Usuario convertToEntity(UsuarioDTO dto) {
        if (dto == null) {
            return null;
        }

        Usuario usuario = new Usuario();
        usuario.setId(dto.getId());
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        
        return usuario;
    }
}