package com.ejemplos.DTO;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the votos database table.
 * 
 */
@Entity
@Table(name="votos")
@NamedQuery(name="Voto.findAll", query="SELECT v FROM Voto v")
public class Voto implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	@Column(name="opcion_seleccionada")
	private int opcionSeleccionada;

	//bi-directional many-to-one association to Votacione
	@ManyToOne
	@JoinColumn(name="votacion_id")
	private Votacione votacione;

	//bi-directional many-to-one association to Usuario
	@ManyToOne
	private Usuario usuario;

	public Voto() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getOpcionSeleccionada() {
		return this.opcionSeleccionada;
	}

	public void setOpcionSeleccionada(int opcionSeleccionada) {
		this.opcionSeleccionada = opcionSeleccionada;
	}

	public Votacione getVotacione() {
		return this.votacione;
	}

	public void setVotacione(Votacione votacione) {
		this.votacione = votacione;
	}

	public Usuario getUsuario() {
		return this.usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

}