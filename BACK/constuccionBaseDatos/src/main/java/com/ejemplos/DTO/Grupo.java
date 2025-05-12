package com.ejemplos.DTO;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the grupos database table.
 * 
 */
@Entity
@Table(name="grupos")
@NamedQuery(name="Grupo.findAll", query="SELECT g FROM Grupo g")
public class Grupo implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	@Column(name="codigo_invitacion")
	private String codigoInvitacion;

	private String nombre;

	//bi-directional many-to-one association to Evento
	@OneToMany(mappedBy="grupo")
	private List<Evento> eventos;

	//bi-directional many-to-one association to Gasto
	@OneToMany(mappedBy="grupo")
	private List<Gasto> gastos;

	//bi-directional many-to-one association to Nota
	@OneToMany(mappedBy="grupo")
	private List<Nota> notas;

	//bi-directional many-to-one association to Usuario
	@OneToMany(mappedBy="grupo")
	private List<Usuario> usuarios;

	//bi-directional many-to-one association to Votacione
	@OneToMany(mappedBy="grupo")
	private List<Votacione> votaciones;

	public Grupo() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCodigoInvitacion() {
		return this.codigoInvitacion;
	}

	public void setCodigoInvitacion(String codigoInvitacion) {
		this.codigoInvitacion = codigoInvitacion;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public List<Evento> getEventos() {
		return this.eventos;
	}

	public void setEventos(List<Evento> eventos) {
		this.eventos = eventos;
	}

	public Evento addEvento(Evento evento) {
		getEventos().add(evento);
		evento.setGrupo(this);

		return evento;
	}

	public Evento removeEvento(Evento evento) {
		getEventos().remove(evento);
		evento.setGrupo(null);

		return evento;
	}

	public List<Gasto> getGastos() {
		return this.gastos;
	}

	public void setGastos(List<Gasto> gastos) {
		this.gastos = gastos;
	}

	public Gasto addGasto(Gasto gasto) {
		getGastos().add(gasto);
		gasto.setGrupo(this);

		return gasto;
	}

	public Gasto removeGasto(Gasto gasto) {
		getGastos().remove(gasto);
		gasto.setGrupo(null);

		return gasto;
	}

	public List<Nota> getNotas() {
		return this.notas;
	}

	public void setNotas(List<Nota> notas) {
		this.notas = notas;
	}

	public Nota addNota(Nota nota) {
		getNotas().add(nota);
		nota.setGrupo(this);

		return nota;
	}

	public Nota removeNota(Nota nota) {
		getNotas().remove(nota);
		nota.setGrupo(null);

		return nota;
	}

	public List<Usuario> getUsuarios() {
		return this.usuarios;
	}

	public void setUsuarios(List<Usuario> usuarios) {
		this.usuarios = usuarios;
	}

	public Usuario addUsuario(Usuario usuario) {
		getUsuarios().add(usuario);
		usuario.setGrupo(this);

		return usuario;
	}

	public Usuario removeUsuario(Usuario usuario) {
		getUsuarios().remove(usuario);
		usuario.setGrupo(null);

		return usuario;
	}

	public List<Votacione> getVotaciones() {
		return this.votaciones;
	}

	public void setVotaciones(List<Votacione> votaciones) {
		this.votaciones = votaciones;
	}

	public Votacione addVotacione(Votacione votacione) {
		getVotaciones().add(votacione);
		votacione.setGrupo(this);

		return votacione;
	}

	public Votacione removeVotacione(Votacione votacione) {
		getVotaciones().remove(votacione);
		votacione.setGrupo(null);

		return votacione;
	}

}