package com.ejemplos.DTO.Votacion;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class VotacionCreateDTO {
	private String pregunta; // Mapea a titulo en la entidad
    private String descripcion; // Opcional
    private List<String> opciones;
    private Date fechaLimite; // Mapea a fechaCierre en la entidad
}