package com.ejemplos.DTO;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class VotacionCreateDTO {
    private String pregunta;
    private List<String> opciones;
    private Long creadaPorId; // ID del usuario que crea la votación
    private Date fechaLimite;

}