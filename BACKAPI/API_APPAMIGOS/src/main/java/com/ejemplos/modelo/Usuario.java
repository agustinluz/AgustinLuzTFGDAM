package com.ejemplos.modelo;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
public class Usuario implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String nombre;
    
    @JsonIgnore
    private String password;

    @OneToMany(mappedBy = "pagadoPor")
    private List<Gasto> gastosPagados;

    @ManyToMany(mappedBy = "usuarios")
    private List<Gasto> gastosParticipados;

    @OneToMany(mappedBy = "usuario")
    private List<Imagen> imagenes;

    @OneToMany(mappedBy = "usuario")
    private List<Nota> notas;

    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    @OneToMany(mappedBy = "usuario")
    private List<Voto> votos;

	public Object getPassword() {
		// TODO Auto-generated method stub
		return password;
	}
	public void setPassword(String password) {
        this.password = password;
    }
}

