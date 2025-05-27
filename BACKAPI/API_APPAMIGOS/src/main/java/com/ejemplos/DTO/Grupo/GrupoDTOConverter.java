package com.ejemplos.DTO.Grupo;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.ejemplos.modelo.Grupo;


@Component
public class GrupoDTOConverter {

    public GrupoDTO convertToDTO(Grupo grupo) {
        GrupoDTO dto = new GrupoDTO();
        dto.setId(grupo.getId());
        dto.setNombre(grupo.getNombre());
        dto.setCodigoInvitacion(grupo.getCodigoInvitacion());
        return dto;
    }

    public Grupo convertToEntity(GrupoCreateDTO createDTO) {
        Grupo grupo = new Grupo();
        grupo.setNombre(createDTO.getNombre());
        grupo.setCodigoInvitacion(UUID.randomUUID().toString().substring(0, 8));
        return grupo;
    }
}
