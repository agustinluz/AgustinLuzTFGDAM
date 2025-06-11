package com.ejemplos.DTO.Evento;

import lombok.Data;

@Data
public class EventoAsistenteDTO {
    private Long usuarioId;
    private String nombre;
    private boolean asistio;
}