package com.ejemplos.DTO;


import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class VotacionDTO {
    private Long id;
    private String pregunta;
    private List<String> opciones;
    private Date fechaCreacion;
    private Long grupoId;
    private String grupoNombre;
    private Long creadaPorId;
    private String creadaPorNombre;
    private int totalVotos;
    private List<VotoResumenDTO> resumenVotos;
    private Date fechaLimite;

}