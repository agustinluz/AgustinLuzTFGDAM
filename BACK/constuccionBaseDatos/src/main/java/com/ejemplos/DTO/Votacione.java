package com.ejemplos.DTO;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;
import java.util.List;


/**
 * The persistent class for the votaciones database table.
 * 
 */
@Entity
@Table(name="votaciones")
@NamedQuery(name="Votacione.findAll", query="SELECT v FROM Votacione v")
public class Votacione implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="fecha_creacion")
	private Date fechaCreacion;

	@Lob
	private String opciones;

	private String pregunta;

	//bi-directional many-to-one association to Grupo
	@ManyToOne
	private Grupo grupo;

	//bi-directional many-to-one association to Voto
	@OneToMany(mappedBy="votacione")
	private List<Voto> votos;

	public Votacione() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getFechaCreacion() {
		return this.fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public String getOpciones() {
		return this.opciones;
	}

	public void setOpciones(String opciones) {
		this.opciones = opciones;
	}

	public String getPregunta() {
		return this.pregunta;
	}

	public void setPregunta(String pregunta) {
		this.pregunta = pregunta;
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
		voto.setVotacione(this);

		return voto;
	}

	public Voto removeVoto(Voto voto) {
		getVotos().remove(voto);
		voto.setVotacione(null);

		return voto;
	}

}