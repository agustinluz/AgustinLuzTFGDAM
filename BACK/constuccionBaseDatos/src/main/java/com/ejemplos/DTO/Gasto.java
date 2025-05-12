package com.ejemplos.DTO;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;


/**
 * The persistent class for the gastos database table.
 * 
 */
@Entity
@Table(name="gastos")
@NamedQuery(name="Gasto.findAll", query="SELECT g FROM Gasto g")
public class Gasto implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	private BigDecimal monto;

	private String titulo;

	//bi-directional many-to-one association to Usuario
	@ManyToOne
	@JoinColumn(name="pagado_por")
	private Usuario usuario;

	//bi-directional many-to-one association to Grupo
	@ManyToOne
	private Grupo grupo;

	//bi-directional many-to-one association to Evento
	@ManyToOne
	private Evento evento;

	//bi-directional many-to-many association to Usuario
	@ManyToMany
	@JoinTable(
		name="gastos_usuarios"
		, joinColumns={
			@JoinColumn(name="gasto_id")
			}
		, inverseJoinColumns={
			@JoinColumn(name="usuario_id")
			}
		)
	private List<Usuario> usuarios;

	public Gasto() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public BigDecimal getMonto() {
		return this.monto;
	}

	public void setMonto(BigDecimal monto) {
		this.monto = monto;
	}

	public String getTitulo() {
		return this.titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public Usuario getUsuario() {
		return this.usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Grupo getGrupo() {
		return this.grupo;
	}

	public void setGrupo(Grupo grupo) {
		this.grupo = grupo;
	}

	public Evento getEvento() {
		return this.evento;
	}

	public void setEvento(Evento evento) {
		this.evento = evento;
	}

	public List<Usuario> getUsuarios() {
		return this.usuarios;
	}

	public void setUsuarios(List<Usuario> usuarios) {
		this.usuarios = usuarios;
	}

}