package com.ejemplos.modelo;

import java.io.Serializable;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Data
@Entity
@Table(name = "grupos")
public class Grupo implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "codigo_invitacion")
    private String codigoInvitacion;
    
    private String nombre;
    
    @Column(name = "imagen_perfil")
    private String imagenPerfil; // URL o path de la imagen
    
    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Usuario admin; // Usuario administrador del grupo
    
    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsuarioGrupo> usuarioGrupos;
    
    @OneToMany(mappedBy = "grupo")
    private List<Evento> eventos;
    
    @OneToMany(mappedBy = "grupo")
    private List<Gasto> gastos;
    
    @OneToMany(mappedBy = "grupo")
    private List<Nota> notas;
    
    @OneToMany(mappedBy = "grupo")
    private List<Votacion> votaciones;
    
    @OneToMany(mappedBy = "grupo")
    private List<Imagen> imagenes;
}