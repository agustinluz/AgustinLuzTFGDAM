package com.ejemplos.DTO;

import java.io.Serializable;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;


/**
 * The persistent class for the eventos database table.
 * 
 */
@Entity
@Table(name="eventos")
@NamedQuery(name="Evento.findAll", query="SELECT e FROM Evento e")
public class Evento implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	@Lob
	private String descripcion;

	@Temporal(TemporalType.TIMESTAMP)
	private Date fecha;

	private String titulo;

	private String ubicacion;

	//bi-directional many-to-one association to Grupo
	@ManyToOne
	private Grupo grupo;

	//bi-directional many-to-one association to Gasto
	@OneToMany(mappedBy="evento")
	private List<Gasto> gastos;

	//bi-directional many-to-one association to Imagene
	@OneToMany(mappedBy="evento")
	private List<Imagene> imagenes;

	public Evento() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescripcion() {
		return this.descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Date getFecha() {
		return this.fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public String getTitulo() {
		return this.titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getUbicacion() {
		return this.ubicacion;
	}

	public void setUbicacion(String ubicacion) {
		this.ubicacion = ubicacion;
	}

	public Grupo getGrupo() {
		return this.grupo;
	}

	public void setGrupo(Grupo grupo) {
		this.grupo = grupo;
	}

	public List<Gasto> getGastos() {
		return this.gastos;
	}

	public void setGastos(List<Gasto> gastos) {
		this.gastos = gastos;
	}

	public Gasto addGasto(Gasto gasto) {
		getGastos().add(gasto);
		gasto.setEvento(this);

		return gasto;
	}

	public Gasto removeGasto(Gasto gasto) {
		getGastos().remove(gasto);
		gasto.setEvento(null);

		return gasto;
	}

	public List<Imagene> getImagenes() {
		return this.imagenes;
	}

	public void setImagenes(List<Imagene> imagenes) {
		this.imagenes = imagenes;
	}

	public Imagene addImagene(Imagene imagene) {
		getImagenes().add(imagene);
		imagene.setEvento(this);

		return imagene;
	}

	public Imagene removeImagene(Imagene imagene) {
		getImagenes().remove(imagene);
		imagene.setEvento(null);

		return imagene;
	}

}