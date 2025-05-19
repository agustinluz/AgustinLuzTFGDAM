package com.ejemplos.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioCreateDTO {
    private String nombre;
    private String email;
    private String password;


}
