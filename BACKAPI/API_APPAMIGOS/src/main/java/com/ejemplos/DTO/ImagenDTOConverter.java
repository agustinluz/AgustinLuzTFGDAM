package com.ejemplos.DTO;

import com.ejemplos.modelo.Imagen;
import org.springframework.stereotype.Component;

@Component
public class ImagenDTOConverter {
    
    public ImagenDTO convertToDTO(Imagen imagen) {
        if (imagen == null) {
            return null;
        }
        
        ImagenDTO dto = new ImagenDTO();
        dto.setId(imagen.getId());
        dto.setNombre(imagen.getNombre());
        dto.setTipoContenido(imagen.getTipoContenido());
        dto.setEventoId(imagen.getEvento() != null ? imagen.getEvento().getId() : null);
        dto.setUsuarioId(imagen.getUsuario() != null ? imagen.getUsuario().getId() : null);
        dto.setGrupoId(imagen.getGrupo() != null ? imagen.getGrupo().getId() : null);
        
        // Si necesitas el nombre del usuario, descomenta esta línea
        // dto.setNombreUsuario(imagen.getUsuario() != null ? imagen.getUsuario().getNombre() : null);
        
        return dto;
    }
}