package com.ejemplos.DTO;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the usuarios database table.
 * 
 */
@Entity
@Table(name="usuarios")
@NamedQuery(name="Usuario.findAll", query="SELECT u FROM Usuario u")
public class Usuario implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	private String email;

	private String nombre;

	private String password;

	//bi-directional many-to-one association to Gasto
	@OneToMany(mappedBy="usuario")
	private List<Gasto> gastos1;

	//bi-directional many-to-many association to Gasto
	@ManyToMany(mappedBy="usuarios")
	private List<Gasto> gastos2;

	//bi-directional many-to-one association to Imagene
	@OneToMany(mappedBy="usuario")
	private List<Imagene> imagenes;

	//bi-directional many-to-one association to Nota
	@OneToMany(mappedBy="usuario")
	private List<Nota> notas;

	//bi-directional many-to-one association to Grupo
	@ManyToOne
	private Grupo grupo;

	//bi-directional many-to-one association to Voto
	@OneToMany(mappedBy="usuario")
	private List<Voto> votos;

	public Usuario() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<Gasto> getGastos1() {
		return this.gastos1;
	}

	public void setGastos1(List<Gasto> gastos1) {
		this.gastos1 = gastos1;
	}

	public Gasto addGastos1(Gasto gastos1) {
		getGastos1().add(gastos1);
		gastos1.setUsuario(this);

		return gastos1;
	}

	public Gasto removeGastos1(Gasto gastos1) {
		getGastos1().remove(gastos1);
		gastos1.setUsuario(null);

		return gastos1;
	}

	public List<Gasto> getGastos2() {
		return this.gastos2;
	}

	public void setGastos2(List<Gasto> gastos2) {
		this.gastos2 = gastos2;
	}

	public List<Imagene> getImagenes() {
		return this.imagenes;
	}

	public void setImagenes(List<Imagene> imagenes) {
		this.imagenes = imagenes;
	}

	public Imagene addImagene(Imagene imagene) {
		getImagenes().add(imagene);
		imagene.setUsuario(this);

		return imagene;
	}

	public Imagene removeImagene(Imagene imagene) {
		getImagenes().remove(imagene);
		imagene.setUsuario(null);

		return imagene;
	}

	public List<Nota> getNotas() {
		return this.notas;
	}

	public void setNotas(List<Nota> notas) {
		this.notas = notas;
	}

	public Nota addNota(Nota nota) {
		getNotas().add(nota);
		nota.setUsuario(this);

		return nota;
	}

	public Nota removeNota(Nota nota) {
		getNotas().remove(nota);
		nota.setUsuario(null);

		return nota;
	}

	public Grupo getGrupo() {
		return this.grupo;
	}

	public void setGrupo(Grupo grupo) {
		this.grupo = grupo;
	}

	public List<Voto> getVotos() {
		return this.votos;
	}

	public void setVotos(List<Voto> votos) {
		this.votos = votos;
	}

	public Voto addVoto(Voto voto) {
		getVotos().add(voto);
		voto.setUsuario(this);

		return voto;
	}

	public Voto removeVoto(Voto voto) {
		getVotos().remove(voto);
		voto.setUsuario(null);

		return voto;
	}

}