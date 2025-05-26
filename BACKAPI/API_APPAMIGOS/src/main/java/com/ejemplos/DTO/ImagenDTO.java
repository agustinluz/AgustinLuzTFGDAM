package com.ejemplos.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImagenDTO {
    private Long id;
    private String nombre;
    private String tipoContenido;
    private Long eventoId;
    private Long usuarioId;
    private Long grupoId;
    private String nombreUsuario; // Campo adicional para mostrar quién subió la imagen
    // Los datos de la imagen se sirven por separado mediante endpoint específico
}