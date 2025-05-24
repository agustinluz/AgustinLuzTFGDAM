package com.ejemplos.DTO;

import com.ejemplos.modelo.Evento;
import org.springframework.stereotype.Component;

@Component
public class EventoDTOConverter {
    
    public EventoDTO convertToDTO(Evento evento) {
        if (evento == null) return null;
        
        EventoDTO dto = new EventoDTO();
        dto.setId(evento.getId());
        dto.setTitulo(evento.getTitulo());
        dto.setDescripcion(evento.getDescripcion());
        dto.setUbicacion(evento.getUbicacion());
        dto.setFecha(evento.getFecha());
        
        if (evento.getGrupo() != null) {
            dto.setGrupoId(evento.getGrupo().getId());
            dto.setGrupoNombre(evento.getGrupo().getNombre());
        }
        
        return dto;
    }
    
    public Evento convertToEntity(EventoCreateDTO dto) {
        if (dto == null) return null;
        
        Evento evento = new Evento();
        evento.setTitulo(dto.getTitulo());
        evento.setDescripcion(dto.getDescripcion());
        evento.setUbicacion(dto.getUbicacion());
        evento.setFecha(dto.getFecha());
        
        return evento;
    }
}