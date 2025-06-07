package com.ejemplos.DTO.Gasto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenDeudaDTO {
    private Long usuarioId;
    private String nombre;
    private String email;
    private double balance; // positivo: le deben, negativo: debe
}
