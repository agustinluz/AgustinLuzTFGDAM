package com.ejemplos.DTO;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventoCreateDTO {
    private String titulo;
    private String descripcion;
    private String ubicacion;
    private Date fecha;


}
