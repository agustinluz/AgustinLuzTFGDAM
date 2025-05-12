package com.ejemplos.DTO;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the imagenes database table.
 * 
 */
@Entity
@Table(name="imagenes")
@NamedQuery(name="Imagene.findAll", query="SELECT i FROM Imagene i")
public class Imagene implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	private String url;

	//bi-directional many-to-one association to Evento
	@ManyToOne
	private Evento evento;

	//bi-directional many-to-one association to Usuario
	@ManyToOne
	private Usuario usuario;

	public Imagene() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Evento getEvento() {
		return this.evento;
	}

	public void setEvento(Evento evento) {
		this.evento = evento;
	}

	public Usuario getUsuario() {
		return this.usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

}